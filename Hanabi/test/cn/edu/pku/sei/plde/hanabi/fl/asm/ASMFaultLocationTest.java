package cn.edu.pku.sei.plde.hanabi.fl.asm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import cn.edu.pku.sei.plde.hanabi.build.proj.ProjectConfig;
import cn.edu.pku.sei.plde.hanabi.build.testrunner.BugsDotJarTestRunner;
import cn.edu.pku.sei.plde.hanabi.fl.ASMFaultLocation;
import cn.edu.pku.sei.plde.hanabi.fl.FaultLocation;
import cn.edu.pku.sei.plde.hanabi.fl.Suspect;
import cn.edu.pku.sei.plde.hanabi.main.Config;

public abstract class ASMFaultLocationTest {

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
	
	protected void innerTest(ProjectConfig projectConfig, Set<String> taraceFiles, String tarceClsName, int patchedLineNum) {
		BugsDotJarTestRunner runner = new BugsDotJarTestRunner(projectConfig);
		
		FaultLocation fl = new ASMFaultLocation(projectConfig, runner);
		
		File traceFolder = new File(Config.ASM_TRACE_FOLDER);
		assertTrue(traceFolder.exists());
		
		File[] files = traceFolder.listFiles();
		
		assertNotNull(files);
		assertTrue(files.length != 0);
		
		int errNum = 0;
		for(File f : files) {
			if(f.getName().endsWith("-false.txt")) {
				errNum++;
				assertTrue(taraceFiles.contains(f.getName()));
			}
		}

		// check failure test number
		assertEquals(taraceFiles.size(), errNum);
		
		List<Suspect> suspects = fl.getAllSuspects();
		
		assertNotNull(suspects);
		assertFalse(suspects.isEmpty());
		
		boolean hit = false;
		
		int rank = 0;
		for(Suspect sus : suspects) {
			rank++;
			System.out.println(sus);
			if(sus.getClassName().equals(tarceClsName) && sus.getLine() == patchedLineNum) {
				hit = true;
			}
		}
		
		assertTrue(hit);
		
		System.out.println(">>>>>>>> BEFORE FILTRT");
		if(rank > 0) {
			System.out.println(">>>>>>>> HIT AT RANK: " + rank);
		} else {
			System.out.println(">>>>>>>> FL MISS");
		}
		
		rank = 0;
		for(Suspect sus : suspects) {
			rank++;
			if(sus.getClassName().equals(tarceClsName) && sus.getLine() == patchedLineNum) {
				break;
			}
		}
		
		System.out.println(">>>>>>>> AFTER FILTRT");
		if(rank > 0 && rank < suspects.size()) {
			System.out.println(">>>>>>>> HIT AT RANK: " + rank);
		} else {
			System.out.println(">>>>>>>> FL MISS");
		}
	}

}
