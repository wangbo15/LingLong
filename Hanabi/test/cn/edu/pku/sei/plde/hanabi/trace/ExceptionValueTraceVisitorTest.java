package cn.edu.pku.sei.plde.hanabi.trace;

import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import cn.edu.pku.sei.plde.hanabi.trace.ExceptionValueTraceVisitor;
import cn.edu.pku.sei.plde.hanabi.utils.FileUtil;
import cn.edu.pku.sei.plde.hanabi.utils.JdtUtil;

public class ExceptionValueTraceVisitorTest {

	private static final String VERSION = "1.7";
	private static final int TYPE = ASTParser.K_COMPILATION_UNIT;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	private void dump(String filePath, int line){
		CompilationUnit cu = (CompilationUnit) JdtUtil.genASTFromSource(FileUtil.readFileToString(filePath), VERSION, TYPE);
		ExceptionValueTraceVisitor visitor = new ExceptionValueTraceVisitor(line, null);
		
		cu.accept(visitor);
		
		System.out.println("//################ INSTRUMENTED ################");
		System.out.println(cu.toString());
	}
	
	@Test
	public void testInstrumentee() {
		String filePath = "./test_resource/instrument/Instrumentee.java";
		//Line 7: if (tmp != 0) 
		int line = 6;	
		dump(filePath, line);
	}

	@Test
	public void testPolygonsSet() {
		String filePath = "./test_resource/instrument/PolygonsSet.java";
		int line = 135;	
		dump(filePath, line);
	}
	
	@Test
	public void testElseIf() {
		String filePath = "./test_resource/instrument/ElseIf.java";
		//Line 15: else if
//		int line = 7;	
//		dump(filePath, line, null);
		
		int line = 15;	
		dump(filePath, line);
	}
	
	@Test
	public void testRet() {
		String filePath = "./test_resource/instrument/Ret.java";		
		int line = 5;	
		dump(filePath, line);
	}
	
	@Test
	public void testSwitchCase() {
		String filePath = "./test_resource/instrument/SwitchCase.java";		
		int line = 13;	
		dump(filePath, line);
	}
}
