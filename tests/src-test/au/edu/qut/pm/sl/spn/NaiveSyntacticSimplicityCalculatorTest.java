package au.edu.qut.pm.sl.spn;

import static org.junit.Assert.*;

import org.junit.Test;

import au.edu.qut.pm.spn_conformance.NaiveSyntacticSimplicityCalculator;

public class NaiveSyntacticSimplicityCalculatorTest {

	@Test
	public void normalizedSimplicity() {
		assertNesEquals(0.0,12,6);
		assertNesEquals(0,1,1);
		assertNesEquals(0.2d,16,20);
		assertNesEquals(0.5d,1,2);
		assertNesEquals(0.9d,10,100);
		assertNesEquals(0.99d,10,1000);
		assertNesEquals(1.0d,5,5000000);
	}

	private void assertNesEquals(double expected, int edges, int events) {
		assertEquals(expected,  
				NaiveSyntacticSimplicityCalculator.normalizedEntitySimplicity(edges, events), 
				0.001d );
	}

}
