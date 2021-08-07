package cn.edu.pku.sei.plde.hanabi.fl.asm.instru;

import java.util.Set;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;


public class TestClassVisitor extends ClassVisitor{
	
	private String className;

	/**
	 * ture: junit3 test case; false: junit4 test case
	 */
	private boolean isJunit3;
	
	private Set<String> skipMtdDescs;
	
	public TestClassVisitor(ClassVisitor cv, boolean isJunit3, Set<String> skipMtdDescs) {
		super(Opcodes.ASM4, cv);
		this.isJunit3 = isJunit3;
		this.skipMtdDescs = skipMtdDescs;
	}
	
	@Override
	public void visit(int version, int access, String name, String signature, String superName,
			String[] interfaces) {
		
		this.className = name.replaceAll("/", ".");
		super.visit(version, access, name, signature, superName, interfaces);
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		MethodVisitor visitor = super.visitMethod(access, name, desc, signature, exceptions);
		
		if("<init>".equals(name) || "<cinit>".equals(name)) {
			return visitor;
		}

		// test methods must be non-static
		boolean isStatic = (access & Opcodes.ACC_STATIC) != 0;
		if(isStatic) {
			return visitor;
		}
		
		String key = TestCaseInstrumenter.generateDesc(name, desc);
		// skip empty methods
		if(skipMtdDescs.contains(key)) {
			
			System.out.println("\tSKIP: " + name);
			
			return visitor;
		}
		
		return new TestMethodVisitor(this.api, visitor, name);
	}

	
	class TestMethodVisitor extends MethodVisitor{
				
		private String mtdName;
		
		private final Label start = new Label();
		private final Label end = new Label();
		private final Label handler = new Label();
		private final Label exit = new Label();
		
		private boolean needInstru = false;
		
		public TestMethodVisitor(int api, MethodVisitor mv, String mtdName) {
			super(api, mv);
			this.mtdName = mtdName;
			
			if(isJunit3 && mtdName.startsWith("test")) {
				this.needInstru = true;
			}
						
		}
		
		@Override
		public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
			if(!isJunit3) {
				//System.out.println("visitAnnotation: " + desc);
				if(desc.equals("Lorg/junit/Test;")) {
					needInstru = true;
				}
			}
			return mv.visitAnnotation(desc, visible);
		}

		
		private void addFinallyBlock() {
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "cn/edu/pku/sei/plde/hanabi/fl/asm/instru/exrt/Tracer", "dump",
					"()V", false);
		}

		@Override
		public void visitCode() {
			mv.visitCode();
			if(!needInstru) {
				return;
			}
			//invoke to Tracer.init(className, methodName);
			mv.visitLdcInsn(TestClassVisitor.this.className);
			mv.visitLdcInsn(this.mtdName);
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "cn/edu/pku/sei/plde/hanabi/fl/asm/instru/exrt/Tracer", "init",
					"(Ljava/lang/String;Ljava/lang/String;)V", false);
			
			mv.visitTryCatchBlock(start, end, handler, null);//"java/lang/Throwable"
			mv.visitLabel(start);
		}

		private boolean visitedRet = false;
		
		@Override
		public void visitInsn(int opcode) {
			if(!needInstru) {
				mv.visitInsn(opcode);
				return;
			}
			
			//check, test case can only have one RET
			if(opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN) {
				if(visitedRet) {
					//throw new Error("MORE THAN ONE RETURN: " + TestClassVisitor.this.className + "#"+ this.mtdName);
				}else{
					visitedRet = true;
				}
			}
			
			if(opcode != Opcodes.RETURN) {
				mv.visitInsn(opcode);
				return;
			}
			mv.visitLabel(end);
			
			//copy finally block
			addFinallyBlock();
			
			//goto the last RET
			mv.visitJumpInsn(Opcodes.GOTO, exit);
			
			mv.visitLabel(handler);	
			
			// store the RuntimeException in local variable
			//mv.visitVarInsn(Opcodes.ASTORE, 1);

			// call setFailure
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "cn/edu/pku/sei/plde/hanabi/fl/asm/instru/exrt/Tracer", "setFailure",
					"()V", false);
			
			//copy finally block
			addFinallyBlock();

			mv.visitInsn(Opcodes.ATHROW);
			
			//the RET instruction
			mv.visitLabel(exit);
			mv.visitInsn(opcode);
		}

		@Override
		public void visitEnd() {
			mv.visitEnd();
		}
		
		@Override
		public void visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack) {
			// TODO Auto-generated method stub
			super.visitFrame(type, nLocal, local, nStack, stack);
		}

		
		
//		@Override
//		public void visitLineNumber(int line, Label start) {
//			
//		}

		@Override
		public void visitMaxs(int maxStack, int maxLocals) {
			if(!needInstru) {
				mv.visitMaxs(maxStack, maxLocals);
				return;
			}
			
//			mv.visitLabel(end);
//			
//			//copy finally block
//			addFinallyBlock();
//			
//			//goto the last RET
//			mv.visitJumpInsn(Opcodes.GOTO, exit);
//			
//			mv.visitLabel(handler);			
//			// call setFailure
//			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "cn/edu/pku/sei/plde/hanabi/fl/asm/instru/exrt/Tracer", "setFailure",
//					"()V", false);
//			
//			//copy finally block
//			addFinallyBlock();

			maxStack = Math.max(maxStack, 3);
			super.visitMaxs(maxStack, maxLocals);
		}
	}// end class TestMethodVisitor

}
