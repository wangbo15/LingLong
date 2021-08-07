package cn.edu.pku.sei.plde.hanabi.trace;

import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import cn.edu.pku.sei.plde.hanabi.trace.AngelicInstruVisitor;
import cn.edu.pku.sei.plde.hanabi.utils.FileUtil;
import cn.edu.pku.sei.plde.hanabi.utils.JdtUtil;

public class AngelicInstruVisitorTest {

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

	private void dump(String filePath, int line, String exctption){
		CompilationUnit cu = (CompilationUnit) JdtUtil.genASTFromSource(FileUtil.readFileToString(filePath), VERSION, TYPE);
		AngelicInstruVisitor visitor = new AngelicInstruVisitor(line, exctption);
		
		cu.accept(visitor);
		
		System.out.println("//################ INSTRUMENTED ################");
		System.out.println(cu.toString());
	}
	
	@Test
	public void testInstrumentee() {
		String filePath = "./test_resource/instrument/Instrumentee.java";
		//Line 7: if (tmp != 0) 
		int line = 7;	
		dump(filePath, line, null);
	}

	@Test
	public void testPolygonsSet() {
		String filePath = "./test_resource/instrument/PolygonsSet.java";
		int line = 136;	
		dump(filePath, line, "java.lang.ClassCastException");
	}
	
	@Test
	public void testElseIf() {
		String filePath = "./test_resource/instrument/ElseIf.java";
		//Line 15: else if
//		int line = 7;	
//		dump(filePath, line, null);
		
		int line = 15;	
		dump(filePath, line, null);
	}
	
	@Test
	public void testSwitchCase() {
		String filePath = "./test_resource/instrument/SwitchCase.java";		
		int line = 15;	
		dump(filePath, line, null);
	}
	
	@Test
	public void testSwitchCase2() {
		String filePath = "./test_resource/instrument/SwitchCase.java";		
		int line = 40;	
		dump(filePath, line, null);
	}
	
	@Test
	public void testUninitialized() {
		String filePath = "./test_resource/instrument/Uninit.java";		
		int line = 10;	
		dump(filePath, line, null);
	}
}
