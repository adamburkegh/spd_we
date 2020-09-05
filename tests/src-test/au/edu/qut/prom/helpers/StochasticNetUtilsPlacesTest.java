package au.edu.qut.prom.helpers;

import static org.junit.Assert.*;

import org.junit.Test;
import org.processmining.models.graphbased.directed.petrinet.StochasticNet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.StochasticNetImpl;

public class StochasticNetUtilsPlacesTest {

	@Test
	public void nullPlaces() {
		assertTrue( StochasticPetriNetUtils.areEqual((Place)null,null) );
		StochasticNet net = new StochasticNetImpl("snupt");
		Place p1 = net.addPlace("p1");
		assertFalse( StochasticPetriNetUtils.areEqual(p1,null) );
	}

	@Test
	public void noEdges() {
		StochasticNet net1 = new StochasticNetImpl("snupt1");
		StochasticNet net2 = new StochasticNetImpl("snupt2");
		Place p1 = net1.addPlace("p1");
		Place p2 = net1.addPlace("p2");
		assertFalse( StochasticPetriNetUtils.areEqual(p1,p2) );
		Place n2p1 = net2.addPlace("p1");
		assertTrue( StochasticPetriNetUtils.areEqual(p1,n2p1) );
	}

	@Test
	public void edgesDiffer() {
		// p1 -> [a] 
		// p2                 (no edge)
		StochasticNet net1 = new StochasticNetImpl("snupt1");
		Place p1 = net1.addPlace("p1");
		Place p2 = net1.addPlace("p2");
		Transition ta = net1.addTransition("a");
		net1.addArc(p1, ta);
		assertFalse( StochasticPetriNetUtils.areEqual(p1,p2) );
		// p1 -> [a] 
		// p2 ->
		StochasticNet net2 = new StochasticNetImpl("snupt2");
		ta = net2.addTransition("a");
		Place p1n2 = net2.addPlace("p1");
		Place p2n2 = net2.addPlace("p2");
		net2.addArc(p1n2, ta);		
		net2.addArc(p2n2, ta);
		assertTrue( StochasticPetriNetUtils.areEqual(p1,p1)  );
		assertTrue( StochasticPetriNetUtils.areEqual(p2,p2)  );
		assertFalse( StochasticPetriNetUtils.areEqual(p1,p2) );
		assertTrue( StochasticPetriNetUtils.areEqual(p1,p1n2) );
		assertTrue( StochasticPetriNetUtils.areEqual(p1n2,p1n2) );
	}

	@Test
	public void sameEdges() {
		// p1 -> [a] 
		// p2                 (no edge)
		StochasticNet net1 = new StochasticNetImpl("snupt1");
		Place p1 = net1.addPlace("p1");
		Place p2 = net1.addPlace("p2");
		Transition ta = net1.addTransition("a");
		net1.addArc(p1, ta);
		net1.addArc(p1, ta);
		assertFalse( StochasticPetriNetUtils.areEqual(p1,p2) );
		// p1 -> [a] 
		// p2 ->
		StochasticNet net2 = new StochasticNetImpl("snupt2");
		ta = net2.addTransition("a");
		Place p1n2 = net2.addPlace("p1");
		Place p2n2 = net2.addPlace("p2");
		net2.addArc(p1n2, ta);		
		assertFalse( StochasticPetriNetUtils.areEqual(p1,p2) );
		assertTrue( StochasticPetriNetUtils.areEqual(p1,p1n2) );
		assertTrue( StochasticPetriNetUtils.areEqual(p2,p2n2) );
	}

	
}
