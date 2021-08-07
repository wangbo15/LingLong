package cn.edu.pku.sei.plde.hanabi.fl;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import cn.edu.pku.sei.plde.hanabi.build.proj.ProjectConfig;
import cn.edu.pku.sei.plde.hanabi.build.proj.ProjectConfigFactory;
import cn.edu.pku.sei.plde.hanabi.build.proj.ProjectConfigFactory.BugType;
import cn.edu.pku.sei.plde.hanabi.fl.FaultLocation;
import cn.edu.pku.sei.plde.hanabi.fl.GZoltarFaultLocation;
import cn.edu.pku.sei.plde.hanabi.fl.OffTheShellFL;
import cn.edu.pku.sei.plde.hanabi.fl.Suspect;

public class FaultLocationTest {

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

	@Test
	public void testOffTheShellFL() {
		BugType projType = BugType.D4J_TYPE;
		String projName = "math_82";
		String root = "/home/nightwish/workspace/defects4j/src/math/" + projName + "_buggy/";
		ProjectConfig projectConfig = ProjectConfigFactory.createPorjectConfig(projType, projName, root);
		
		FaultLocation fl = new OffTheShellFL(projectConfig);
		
		List<Suspect> res = fl.getAllSuspects();
		for(int i = 0; i < res.size(); i++) {
			Suspect sus = res.get(i);
			System.out.println(i + "\t" + sus);
		}
	}
	
	@Test
	public void testGZoltarFaultLocation() {
		BugType projType = BugType.D4J_TYPE;
		String projName = "math_46";
		String root = "/home/nightwish/workspace/defects4j/src/math/" + projName + "_buggy/";
		ProjectConfig projectConfig = ProjectConfigFactory.createPorjectConfig(projType, projName, root);
		
		FaultLocation fl = new GZoltarFaultLocation(projectConfig);
		
		List<Suspect> res = fl.getAllSuspects();
		for(int i = 0; i < res.size(); i++) {
			Suspect sus = res.get(i);
			System.out.println(i + "\t" + sus);
		}

	}
	

	@Test
	public void testGZoltarFaultLocation_Math26() {
		BugType projType = BugType.D4J_TYPE;
		String projName = "math_26";
		String root = "/home/nightwish/workspace/defects4j/src/math/" + projName + "_buggy/";
		ProjectConfig projectConfig = ProjectConfigFactory.createPorjectConfig(projType, projName, root);
		
//		FaultLocation fl = new OffTheShellFL(projectConfig);
//		
//		List<Suspect> res = fl.getAllSuspects();
//		
//		for(Suspect sus : res){
//			System.out.println(sus);
//		}
		
		FaultLocation fl = new GZoltarFaultLocation(projectConfig);
		
		List<Suspect> res = fl.getAllSuspects();
		for(int i = 0; i < res.size(); i++) {
			Suspect sus = res.get(i);
			System.out.println(i + "\t" + sus);
		}

	}
	
	@Test
	public void testOffTheShellFL_Math35() {
		BugType projType = BugType.D4J_TYPE;
		String projName = "math_35";
		String root = "/home/nightwish/workspace/defects4j/src/math/" + projName + "_buggy/";
		ProjectConfig projectConfig = ProjectConfigFactory.createPorjectConfig(projType, projName, root);
		
		FaultLocation fl = new OffTheShellFL(projectConfig);
		
		List<Suspect> res = fl.getAllSuspects();
		
		boolean hit0 = false;
		boolean hit1 = false;
		for(Suspect sus : res){
			System.out.println(sus);
			if(sus.getClassName().equals("org.apache.commons.math3.genetics.ElitisticListPopulation") && sus.getLine() == 52) {
				hit0 = true;
			}
			if(sus.getClassName().equals("org.apache.commons.math3.genetics.ElitisticListPopulation") && sus.getLine() == 66) {
				hit1 = true;
			}
		}
		assertTrue(hit0);
		assertTrue(hit1);		
	}
	
	@Test
	public void testGZoltarFaultLocation_Math35() {
		BugType projType = BugType.D4J_TYPE;
		String projName = "math_35";
		String root = "/home/nightwish/workspace/defects4j/src/math/" + projName + "_buggy/";
		ProjectConfig projectConfig = ProjectConfigFactory.createPorjectConfig(projType, projName, root);
		
		FaultLocation fl = new GZoltarFaultLocation(projectConfig);
		
		List<Suspect> res = fl.getAllSuspects();
		
		assertEquals(res.size(), 2);
		
		Suspect hit0 = null;
		Suspect hit1 = null;
		for(Suspect sus : res){
			System.out.println(sus);			
			if(sus.getClassName().equals("org.apache.commons.math3.genetics.ElitisticListPopulation") && sus.getLine() == 52) {
				hit0 = sus;
			}
			if(sus.getClassName().equals("org.apache.commons.math3.genetics.ElitisticListPopulation") && sus.getLine() == 66) {
				hit1 = sus;
			}
		}
		assertNotNull(hit0);
		assertEquals(hit0.getTriggerTests().size(), 2);

		assertNotNull(hit1);
		assertEquals(hit1.getTriggerTests().size(), 2);

	}
	
	@Test
	public void testOffTheShellFL_Math4() {
		BugType projType = BugType.D4J_TYPE;
		String projName = "math_4";
		String root = "/home/nightwish/workspace/defects4j/src/math/" + projName + "_buggy/";
		ProjectConfig projectConfig = ProjectConfigFactory.createPorjectConfig(projType, projName, root);
		
		FaultLocation fl = new OffTheShellFL(projectConfig);
		
		List<Suspect> res = fl.getAllSuspects();
		
		boolean hit0 = false;
		boolean hit1 = false;
		for(Suspect sus : res){
			System.out.println(sus);
			if(sus.getClassName().equals("org.apache.commons.math3.geometry.euclidean.threed.SubLine") && sus.getLine() == 113) {
				hit0 = true;
			}
			if(sus.getClassName().equals("org.apache.commons.math3.geometry.euclidean.twod.SubLine") && sus.getLine() == 117) {
				hit1 = true;
			}
		}
		assertTrue(hit0);
		assertTrue(hit1);		
	}

	
}
