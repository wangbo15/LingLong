package edu.pku.sei.conditon.simple.expr;

import static org.junit.Assert.*;

import org.junit.Test;

public class TypeFilterTest {

	@Test
	public void test() {
		String type = "E";
		assertEquals(TypeFilter.filtType(type) , "Object");
		
		type = "AbstractListChromosome<?>";
		assertEquals(TypeFilter.filtType(type) , "AbstractListChromosome");

		type = "AbstractListChromosome<T>";
		assertEquals(TypeFilter.filtType(type) , "AbstractListChromosome");
		
		type = "Iterator<SubHyperplane<S>>";
		assertEquals(TypeFilter.filtType(type) , "Iterator<SubHyperplane>");
		
		type = "Comparator<Comparable<?>>";
		assertEquals(TypeFilter.filtType(type) , "Comparator<Comparable>");


	}

}
