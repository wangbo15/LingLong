package org.pkg;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Make sure you are in the folder:
 * Hanabi/test_resource/
 * 
 * How to compile: 
 * javac -cp /home/nightwish/program_files/eclipse/plugins/org.junit_4.12.0.v201504281640/junit.jar:. -g ./org/pkg/HelloTest.java
 *
 * IN DELL:
 * javac -cp .:/home/nightwish/program_files/eclipse-jee-oxygen/plugins/org.junit_4.12.0.v201504281640/junit.jar:/home/nightwish/program_files/eclipse-jee-oxygen/plugins/org.hamcrest.core_1.3.0.v201303031735.jar  org/pkg/HelloTest.java
 * 
 * How to run test:
 * java -cp .:/home/nightwish/program_files/eclipse/plugins/org.junit_4.12.0.v201504281640/junit.jar:/home/nightwish/program_files/eclipse/plugins/org.hamcrest.core_1.3.0.v201303031735.jar org.junit.runner.JUnitCore org.pkg.HelloTest
 * 
 * IN DELL:
 * java -cp .:/home/nightwish/workspace/eclipse/Hanabi/bin/:/home/nightwish/program_files/eclipse-jee-oxygen/plugins/org.junit_4.12.0.v201504281640/junit.jar:/home/nightwish/program_files/eclipse-jee-oxygen/plugins/org.hamcrest.core_1.3.0.v201303031735.jar -XX:-UseSplitVerifier org.junit.runner.JUnitCore org.pkg.HelloTest
 */
public class HelloTest {

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
	public void testFoo() {
		assertEquals(Hello.foo(1, 2), 3);
		assertEquals(Hello.foo(0, 1), -1);
	}
	
	/*
	@Test
	public void testFoo_Instru() {
		Tracer.init();
		try {
			assertEquals(Hello.foo(1, 2), 3);
			assertEquals(Hello.foo(0, 1), -1);
		} catch (Throwable t) {
			Tracer.setFailure()
			t.printStackTrace();
		} finally {
			Tracer.dump();
		}
	}*/
	
	
	@Test
	public void testDevide() {
		assertEquals(Hello.devide(1, 2), 0);
		assertEquals(Hello.devide(3, 2), 1);

		try {
			Hello.devide(10, 0);
			fail("An ArithmeticException wanted");
		} catch(ArithmeticException e) {

		}
	}

}
