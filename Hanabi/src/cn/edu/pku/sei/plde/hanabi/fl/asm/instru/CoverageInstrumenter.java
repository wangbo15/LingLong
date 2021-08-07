package cn.edu.pku.sei.plde.hanabi.fl.asm.instru;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.ListIterator;
import java.util.Set;
import java.util.Stack;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

import cn.edu.pku.sei.plde.hanabi.utils.FileUtil;
import javassist.bytecode.Opcode;

public class CoverageInstrumenter extends Instrumenter{
	
	private static AbstractInsnNode pushIntegerConstToStack(int val) {
		switch(val) {
		case 0:
			return new InsnNode(Opcodes.ICONST_0);
		case 1:
			return new InsnNode(Opcodes.ICONST_1);
		case 2:
			return new InsnNode(Opcodes.ICONST_2);
		case 3:
			return new InsnNode(Opcodes.ICONST_3);
		case 4:
			return new InsnNode(Opcodes.ICONST_4);
		case 5:
			return new InsnNode(Opcodes.ICONST_5);
		default:
			if(val <= 127 && val >= -128) {
				return new IntInsnNode(Opcodes.BIPUSH, val);
			}else {
				return new IntInsnNode(Opcodes.SIPUSH, val);
			}
		
		}
	}
	
	private static InsnList getInstrumentInstList(String fileName, int line) {
		InsnList il = new  InsnList();
		
		il.add(new LdcInsnNode(fileName));
		il.add(pushIntegerConstToStack(line));
		MethodInsnNode invokTrace = new MethodInsnNode(Opcodes.INVOKESTATIC, 
				"cn/edu/pku/sei/plde/hanabi/fl/asm/instru/exrt/Tracer", 
				"trace", 
				"(Ljava/lang/String;I)V", 
				false);
		il.add(invokTrace);
		return il;
	}
	
	private static void traverseMethodsToInstru(ClassNode classNode) {
		
		String fileName = classNode.name.replaceAll("/", ".");
		
		for (Object obj : classNode.methods) {
			MethodNode methodNode = (MethodNode) obj;
			
			// skip constructors
			if("<init>".equals(methodNode.name) || "<cinit>".equals(methodNode.name)) {
				continue;
			}
			
//			if(!methodNode.name.equals("setProperty")) {
//				continue;
//			}
			
			InsnList lists = methodNode.instructions;
            ListIterator<AbstractInsnNode> it = lists.iterator();
            
            int currLine = -1;

            Set<Integer> skipLines = getSkipLines(lists);
            int insertTime = 0;
            
			/**
			 * All we needed was to make sure that if the label visit was originally
			 * followed by a frame visit, we would insert our instructions after the frame
			 * visit, not between the frame and the label.
			 */
            AbstractInsnNode insertPoint = null;
            while (it.hasNext()) {
                AbstractInsnNode node = it.next();
                
                if (node instanceof LineNumberNode) {
                	LineNumberNode lnNode = (LineNumberNode) node;
                	
                	currLine = lnNode.line;
                	
                	if(skipLines.contains(currLine)) {
                		continue;
                	}
                	
                	// code example to skip lines
//                	if(currLine != 199) {
//                		continue;
//                	}
                    //System.out.println(">>>>>>>> " + currLine);

                	if(node.getNext() instanceof FrameNode) {
                    	insertPoint = node.getNext();
                    } else {
                    	insertPoint = node;
                    }
                }
                
                if(node == insertPoint) { // && !skipCurrLine) {
                    InsnList traceInsns = getInstrumentInstList(fileName, currLine);
                    lists.insert(insertPoint, traceInsns);
                    
                    insertTime++;
                }
                
            }// end while
            
            //modify max stack
        	methodNode.maxStack = Math.max(2, methodNode.maxStack);
            
		}//end for (Object obj : classNode.methods)
	}
	
	/**
	 * filter triple expression "aaa ? bbb : ccc" in an init,
	 * such as:
	 * 		new SimpleToken(new SimpleTokenType(TokenType.character, sb.toString()), index, special ? 2 : 1);
	 *      throw new IllegalArgumentException("msg" + (value != null ? value.getClass().getCanonicalName() : "[null]");
	 * 
	 * @param lists
	 * @return
	 */
	private static Set<Integer> getSkipLines(InsnList lists) {
		Set<Integer> result = new HashSet<>();
		
        ListIterator<AbstractInsnNode> it = lists.iterator();
        
        int currLine = -1;
        boolean hasJump = false;        
        
        Stack<String> newClassStack = new Stack<>();

        
        while (it.hasNext()) {
            AbstractInsnNode node = it.next();
            
            if (node instanceof LineNumberNode) {
            	LineNumberNode lnNode = (LineNumberNode) node;
            	currLine = lnNode.line;
            	hasJump = false;
            	newClassStack.clear();
        		continue;
            }
            
            if(node instanceof TypeInsnNode) {
            	TypeInsnNode tpNode = (TypeInsnNode) node;
            	if(tpNode.getOpcode() == Opcode.NEW) {
            		newClassStack.push(tpNode.desc);
            		//System.out.println("PUSH: " + tpNode.desc);
            	}
            	continue;
            	
            }
            
            if (node instanceof JumpInsnNode) {
            	JumpInsnNode jpNode = (JumpInsnNode) node;
            	if(jpNode.getOpcode() != Opcode.GOTO) {
            		hasJump = true;
            	}
            	continue;
            }
            
            if(node instanceof MethodInsnNode) {
            	MethodInsnNode mtNode = (MethodInsnNode) node;
            	if(mtNode.getOpcode() != Opcode.INVOKESPECIAL) {
            		continue;
            	}
            	if(!newClassStack.isEmpty()) {
            		String previousNew = newClassStack.pop();
                	String owner = mtNode.owner;
                	//System.out.println("POP: " + previousNew);
                	//System.out.println("DESC: " + mtNode.desc + " OWNER: " + mtNode.owner);
            		if(owner.equals(previousNew) && hasJump) {
            			result.add(currLine);
            		}
            	}
            }
           
        }// end while
        
		return result;
	}

	public static void instrument(File classFile) {
		InputStream in = null;
		try {
			in = new FileInputStream(classFile);
			ClassReader cr = new ClassReader(in);
			ClassNode classNode = new ClassNode();
			
			// ClassNode is a ClassVisitor
			cr.accept(classNode, 0);
			
			traverseMethodsToInstru(classNode);
			
			ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);		
			classNode.accept(cw);
			
			byte[] bytes = cw.toByteArray();
			rewrite(classFile, bytes);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			FileUtil.safeCloseCloseables(in);
		}
		
	}
}
