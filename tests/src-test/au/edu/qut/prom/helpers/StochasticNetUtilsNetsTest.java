package au.edu.qut.prom.helpers;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.processmining.models.graphbased.directed.petrinet.StochasticNet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.StochasticNetImpl;

public class StochasticNetUtilsNetsTest {

	private PetriNetFragmentParser parser = new PetriNetFragmentParser();
	
	@Before
	public void setUp() {
		parser = new PetriNetFragmentParser();
	}
	
	public void areEqual(StochasticNet net1, StochasticNet net2) {
		assertTrue( StochasticPetriNetUtils.areEqual(net1, net2) );
	}
	
	public void areNotEqual(StochasticNet net1, StochasticNet net2) {
		assertFalse( StochasticPetriNetUtils.areEqual(net1, net2) );
	}
	
	@Test
	public void compareNulls() {
		areEqual(null, null);
		areNotEqual(new StochasticNetImpl("empty"), null);
		areNotEqual(null, new StochasticNetImpl("empty") );
	}

	@Test
	public void compareOneTransition() {
		// I -> [a] -> F
		StochasticNet net1 = new StochasticNetImpl("ot1");
		Transition ta = net1.addTransition("a");
		Place initialPlace = net1.addPlace("initial");
		Place finalPlace = net1.addPlace("final");
		net1.addArc(initialPlace, ta);
		net1.addArc(ta,finalPlace);
		StochasticNet net2 = new StochasticNetImpl("ot2");
		ta = net2.addTransition("a");
		initialPlace = net2.addPlace("initial");
		finalPlace = net2.addPlace("final");
		net2.addArc(initialPlace, ta);
		net2.addArc(ta,finalPlace);
		areEqual(net1,net2);
		areEqual(net2,net1);
	}
		

	@Test
	public void compareOneTimedTransition() {
		StochasticNet net1 = parser.createNet("cott", "I -> {a 1.0} -> F");
		StochasticNet net2 = parser.createNet("cott2", "I -> {a 1.0} -> F");
		StochasticNet net3 = parser.createNet("cott3", "I -> {a 0.8} -> F");
		areEqual(net1,net1);
		areEqual(net2,net2);
		areEqual(net1,net2);
		areNotEqual(net1,net3);
		areNotEqual(net2,net3);
	}
	
	
}
