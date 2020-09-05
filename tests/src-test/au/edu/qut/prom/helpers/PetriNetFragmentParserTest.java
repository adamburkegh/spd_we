package au.edu.qut.prom.helpers;

import static org.junit.Assert.*;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.models.graphbased.directed.petrinet.StochasticNet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.StochasticNetImpl;
import org.processmining.models.semantics.petrinet.Marking;

public class PetriNetFragmentParserTest {

	private PetriNetFragmentParser parser = null;
	
	@Before
	public void setUp() {
		parser = new PetriNetFragmentParser();		
	}
	
	@Test(expected = RuntimeException.class)
	public void invalidInput() {
		parser.createNet("invalid", "I --> ~jerry [a] -> F");
	}

	
	@Test
	public void ptpFragment() {
		StochasticNet expected = new StochasticNetImpl("expected");
		Place initialPlace = expected.addPlace("I");
		Transition ta = expected.addTransition("a");
		Place finalPlace = expected.addPlace("F");
		expected.addArc(initialPlace, ta);
		expected.addArc(ta,finalPlace);
		PetriNetFragmentParser parser = new PetriNetFragmentParser();
		StochasticNet net = new StochasticNetImpl("pf");
		parser.addToNet(net, "I -> [a] -> F");
		assertTrue( StochasticPetriNetUtils.areEqual(expected, net) );
	}

	@Test
	public void weightedTransition() {
		StochasticNet expected = new StochasticNetImpl("expected");
		Place initialPlace = expected.addPlace("I");
		Transition ta = expected.addImmediateTransition("a", 0.4d);
		Place finalPlace = expected.addPlace("F");
		expected.addArc(initialPlace, ta);
		expected.addArc(ta,finalPlace);
		PetriNetFragmentParser parser = new PetriNetFragmentParser();
		StochasticNet net = new StochasticNetImpl("pf");
		parser.addToNet(net, "I -> {a 0.4} -> F");
		assertTrue( StochasticPetriNetUtils.areEqual(expected, net) );
	}

	@Test
	public void weightedTransitionAboveTen() {
		StochasticNet expected = new StochasticNetImpl("expected");
		Place initialPlace = expected.addPlace("I");
		Transition ta = expected.addImmediateTransition("a", 10.4d);
		Place finalPlace = expected.addPlace("F");
		expected.addArc(initialPlace, ta);
		expected.addArc(ta,finalPlace);
		PetriNetFragmentParser parser = new PetriNetFragmentParser();
		StochasticNet net = new StochasticNetImpl("pf");
		parser.addToNet(net, "I -> {a 10.4} -> F");
		assertTrue( StochasticPetriNetUtils.areEqual(expected, net) );
	}

	
	@Test
	public void weightedTransitionDefaultWeight() {
		StochasticNet expected = new StochasticNetImpl("expected");
		Place initialPlace = expected.addPlace("I");
		Transition ta = expected.addImmediateTransition("a", 1.0d);
		Place finalPlace = expected.addPlace("F");
		expected.addArc(initialPlace, ta);
		expected.addArc(ta,finalPlace);
		PetriNetFragmentParser parser = new PetriNetFragmentParser();
		StochasticNet net = new StochasticNetImpl("pf");
		parser.addToNet(net, "I -> {a} -> F");
		assertTrue( StochasticPetriNetUtils.areEqual(expected, net) );
	}

	
	@Test
	public void twoTransitionFragment() {
		StochasticNet expected = new StochasticNetImpl("expected");
		Place initialPlace = expected.addPlace("initialPlace");
		Transition t1 = expected.addTransition("transition1");
		Place mp = expected.addPlace("mp");
		Transition t2 = expected.addTransition("transition2");
		Place finalPlace = expected.addPlace("finalPlace");
		expected.addArc(initialPlace, t1);
		expected.addArc(t1, mp);
		expected.addArc(mp,t2);
		expected.addArc(t2,finalPlace);
		StochasticNet net = parser.createNet("ttf", 
				"initialPlace -> [transition1] -> mp -> [transition2] -> finalPlace");
		assertTrue( StochasticPetriNetUtils.areEqual(expected, net) );
	}

	@Test
	public void multiEdge() {
		StochasticNet expected = new StochasticNetImpl("expected");
		Place initialPlace = expected.addPlace("I");
		Transition ta = expected.addTransition("a");
		Transition tb = expected.addTransition("b");
		Place finalPlace = expected.addPlace("F");
		expected.addArc(initialPlace, ta);
		expected.addArc(initialPlace, tb);
		expected.addArc(ta,finalPlace);
		expected.addArc(tb,finalPlace);
		PetriNetFragmentParser parser = new PetriNetFragmentParser();
		StochasticNet net = new StochasticNetImpl("pf");
		// This is equivalent to a single net
		//     [a] 
		// I -/   \-> F
		//    \[b]/
		parser.addToNet(net, "I -> [a] -> F");
		parser.addToNet(net, "I -> [b] -> F");
		assertTrue( StochasticPetriNetUtils.areEqual(expected, net) );
	}

	@Test
	public void acceptingPetrinetWeighted() {
		StochasticNet expected = new StochasticNetImpl("expected");
		Place initialPlace = expected.addPlace("I");
		Transition ta = expected.addImmediateTransition("a", 0.4d);
		Place finalPlace = expected.addPlace("F");
		expected.addArc(initialPlace, ta);
		expected.addArc(ta,finalPlace);
		PetriNetFragmentParser parser = new PetriNetFragmentParser();
		AcceptingPetriNet result = parser.createAcceptingNet("an", "I -> {a 0.4} -> F");
		assertTrue( StochasticPetriNetUtils.areEqual(expected, (StochasticNet)result.getNet()) );
		checkMarkings(result, "I", "F");
	}

	private void checkMarkings(AcceptingPetriNet result, String initialLabel, String finalLabel) {
		Marking resultInit = result.getInitialMarking();
		assertEquals(1, resultInit.size() );
		for (Place place: resultInit) {
			assertEquals(initialLabel, place.getLabel());
		}
		Set<Marking> resultFinal = result.getFinalMarkings();
		assertEquals(1, resultFinal.size() );
		for (Marking finalMarking: resultFinal) {
			for (Place place: finalMarking) {
				assertEquals(finalLabel, place.getLabel());
			}
		}
	}

	@Test
	public void acceptingMultiEdge() {	
		StochasticNet expected = new StochasticNetImpl("expected");
		Place initialPlace = expected.addPlace("Start");
		Transition ta = expected.addTransition("a");
		Transition tb = expected.addTransition("b");
		Place finalPlace = expected.addPlace("End");
		expected.addArc(initialPlace, ta);
		expected.addArc(initialPlace, tb);
		expected.addArc(ta,finalPlace);
		expected.addArc(tb,finalPlace);
		PetriNetFragmentParser parser = new PetriNetFragmentParser();
		// This is equivalent to a single net
		//     [a] 
		// I -/   \-> F
		//    \[b]/
		AcceptingPetriNet result = parser.createAcceptingNet("pf", "Start -> [a] -> End");
		parser.addToAcceptingNet(result, "Start -> [b] -> End");
		assertTrue( StochasticPetriNetUtils.areEqual(expected, (StochasticNet)result.getNet()) );
		checkMarkings(result, "Start", "End");
	}

	
}
