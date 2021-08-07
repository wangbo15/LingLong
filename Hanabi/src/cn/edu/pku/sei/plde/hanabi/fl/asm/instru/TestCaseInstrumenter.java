package cn.edu.pku.sei.plde.hanabi.fl.asm.instru;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.ListIterator;
import java.util.Set;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodNode;

import cn.edu.pku.sei.plde.hanabi.utils.FileUtil;

public class TestCaseInstrumenter extends Instrumenter{
	
	public static void instrument(File classFile, boolean isJunit3) {
		
		Set<String> skipMtdDescs = getSkippedTestMtd(classFile, isJunit3);
		
		InputStream in = null;
		try {
			in = new FileInputStream(classFile);
			
			ClassReader cr = new ClassReader(in);
			
			//choose the opt implementation, instead of new ClassWriter(0);
			ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES);

			TestClassVisitor ca = new TestClassVisitor(cw, isJunit3, skipMtdDescs); 
			
			cr.accept(ca, ClassReader.EXPAND_FRAMES);
			
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
	
	public static String generateDesc(String mtdName, String oriDesc) {
		assert mtdName != null && oriDesc != null;
		return mtdName + "#" + oriDesc;
	}
	
	private static Set<String> getSkippedTestMtd(File classFile, boolean isJunit3) {
		Set<String> skippedMtdDescs = new HashSet<>();
		
		InputStream in = null;
		try {
			in = new FileInputStream(classFile);

			ClassReader cr = new ClassReader(in);
			ClassNode classNode = new ClassNode();

			cr.accept(classNode, 0);
			
			for (Object obj : classNode.methods) {
				MethodNode methodNode = (MethodNode) obj;
				String name = methodNode.name;
				
				String desc = generateDesc(name, methodNode.desc);
				if("<init>".equals(name) || "<cinit>".equals(methodNode.name)) {
					skippedMtdDescs.add(desc);
					continue;
				}
				
				// skip static method
				if((methodNode.access & Opcodes.ACC_STATIC) != 0) {
					skippedMtdDescs.add(desc);
					continue;
				}
				
				//remain test functions
				if(!name.startsWith("test")) {
	            	skippedMtdDescs.add(desc);
					continue;
				}
				
				// add support for try-catch test in Junit3
				// skip method that already contains try-catch 
	            if (isJunit3 && !methodNode.tryCatchBlocks.isEmpty()) {
	            	skippedMtdDescs.add(desc);
	            	continue;
	            }
				
				InsnList lists = methodNode.instructions;
	            ListIterator<AbstractInsnNode> it = lists.iterator();
	            
	            int insnSize = 0;
	            while(it.hasNext()) {
	            	AbstractInsnNode node = it.next();
	            	if(node instanceof LabelNode || node instanceof LineNumberNode) {
	            		continue;
	            	}
	            	insnSize++;
	            }
	            
	            if(insnSize <= 1) {
	            	skippedMtdDescs.add(desc);
	            }

			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			FileUtil.safeCloseCloseables(in);
		}
		return skippedMtdDescs;
	}
}