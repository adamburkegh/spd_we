package au.edu.qut.pm.spn_estimator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.processmining.models.graphbased.directed.petrinet.StochasticNet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

import au.edu.qut.pm.spn_estimator.ActivityPairRHEstimator;
import au.edu.qut.prom.helpers.PetriNetFragmentParser;
import au.edu.qut.prom.helpers.StochasticPetriNetUtils;
import au.edu.qut.prom.helpers.StochasticTestUtils;

public class ActivityPairRHTest {

	private static Logger LOGGER = LogManager.getLogger();
	
	private PetriNetFragmentParser parser = new PetriNetFragmentParser();
	
	@BeforeClass
	public static void beforeClass() {
		StochasticTestUtils.initializeLogging();
	}
	
	@Before
	public void setUp() {
		parser = new PetriNetFragmentParser();
	}

	private static StochasticNet estimate(StochasticNet minedModel, String ... traces) {
		return StochasticTestUtils.estimateFromTraces(minedModel,new ActivityPairRHEstimator(),traces);
	}
	
	private StochasticNet estimateWithDefault(StochasticNet expected, String ... traces) {
		return StochasticTestUtils.estimateWithDefault(expected,new ActivityPairRHEstimator(),traces);
	}

	private static void checkEqual(String message, StochasticNet expected, StochasticNet net) {
		StochasticTestUtils.checkEqual(LOGGER,message,expected,net);
	}
	
	@Test
	public void singleTransition() {
		StochasticNet expected = parser.createNet("expected", 
							"Start -> {a 2.0} -> End");
		StochasticNet net = estimateWithDefault(expected,"a");
		checkEqual( "single transition", expected, net);
		assertEquals(2,net.getEdges().size());
		assertEquals(2,net.getPlaces().size());
		Collection<Transition> transitions = net.getTransitions();
		assertEquals(1,transitions.size());
		Transition transition = transitions.iterator().next();
		assertEquals("a", transition.getLabel());
		assertEquals(1,transition.getGraph().getInEdges(transition).size());
		assertEquals(1,transition.getGraph().getOutEdges(transition).size());
		StochasticTestUtils.assertWeightsEqual(2.0d,transition);
	}

	@Test
	public void twoSequentialTransitions() {
		StochasticNet expected = parser.createNet("expected", 
				"Start -> {a 2.0} -> p1 -> {b} -> End");
		StochasticNet net = estimateWithDefault(expected,"a b");
		assertEquals(4,net.getEdges().size());
		assertEquals(3,net.getPlaces().size());
		Collection<Transition> transitions = net.getTransitions();
		assertEquals(2,transitions.size());
		checkEqual("two seq", expected, net);
		Set<String> expectedLabels = new HashSet<String>();
		expectedLabels.add("a"); expectedLabels.add("b");
		for (Transition transition: transitions) {
			assertTrue(expectedLabels.contains(transition.getLabel()));
			assertEquals(1,transition.getGraph().getInEdges(transition).size());
			assertEquals(1,transition.getGraph().getOutEdges(transition).size());
		}
	}


	@Test
	public void twoSequentialTransitionsTwoEvents() {
		StochasticNet expected = parser.createNet("expected", 
				"Start -> {a 4.0} -> p1 -> {b 2.0} -> End");
		StochasticNet net = estimateWithDefault(expected,"a b","a b");
		checkEqual("two seq two events", expected, net);
		// Interesting property - pure frequency estimators given different weights 
		// (linearly related) to duplicated 
		// traces ... eg the same log repeated results in doubling all the weights
	}
	
	@Test
	public void immediateChoice() {
		StochasticNet expected = parser.createNet("expected", 
				  					"Start -> {a 2.0} -> p1 -> {c 2.0} -> End");
		parser.addToNet(expected,   "Start -> {b 2.0} -> p2 -> {c 2.0} -> End");
		// a = 1 (start) + 1 (a c) + 0 (end)
		// b = 1 (start) + 1 (b c) + 0 (end)
		// c = 0 (start)           + 1 (end)
		StochasticNet net = estimateWithDefault(expected,"a b c",
														 "b a c");
		checkEqual("one choice", expected, net);
	}
	
	@Test
	public void oneInlineChoiceUnevenWeights() {
		StochasticNet expected = parser.createNet("expected", 
				  "Start -> {a 8.0} -> p1 -> {b 3.0} -> p2 -> {d 4.0} -> End");
		parser.addToNet(expected,     "p1 -> {c 1.0} -> p2");
		StochasticTestUtils.renamePlacesByTransition(expected);
		StochasticNet net = estimateWithDefault(expected,
									"a b d","a b d","a b d",
									"a c d");
		assertTrue( StochasticPetriNetUtils.areEqual(expected, net) );
	}


	@Test
	public void inlineChoice() {
		StochasticNet expected = parser.createNet("expected", 
				  "Start -> {a 4.0} -> p1 -> {b 1.0} -> p2 -> {d 2.0} -> End");
		parser.addToNet(expected,     "p1 -> {c 1.0} -> p2");
		StochasticNet net = estimateWithDefault(expected,"a b d",
														 "a c d");
		assertTrue( StochasticPetriNetUtils.areEqual(expected, net) );
	}
	
	
	@Test
	public void selfLoop() {
		StochasticNet expected = parser.createNet("expected", 
				  "Start -> {a 4.0} -> p1 -> {b 3.0} -> p2 -> {d 2.0} -> End");
		parser.addToNet(expected,     "p1 -> {b 3.0} -> p3 -> {b 3.0} -> p3");
		StochasticNet net = estimateWithDefault(expected,"a b d",
														 "a b b d");
		// known alpha classic limitation on self-loops
		checkEqual("self loop", expected, net);
	}
	
	@Test
	public void twoLoop() {
		StochasticNet expected = parser.createNet("expected", 
				  "Start -> {a 4.0} -> p1 -> {b 3.0} -> End");
		parser.addToNet(expected,     "p1 -> {b 3.0} -> p2 -> {c 1.0} -> p3 -> {b 3.0} -> End");
		StochasticNet net = estimateWithDefault(expected,
									"a b",
									"a b c b");
		checkEqual("two loop", expected, net);
	}

	@Test
	public void threeLoop() {
		StochasticNet expected = parser.createNet("expected", 
				  "Start -> {a 4.0} -> p1 -> {b 3.0} -> End");
		parser.addToNet(expected,     "p1 -> {b 3.0} -> p2 -> {c 1.0} -> p3 -> {d 1.0} -> p1");
		StochasticNet net = estimateWithDefault(expected,
									"a b",
									"a b c d b");
		checkEqual("three loop", expected, net);
	}


	@Test
	public void choiceDivergentBeforeFinal() {
		StochasticNet expected = parser.createNet("expected", 
				  "Start -> {a 4.0} -> p1 -> {b 1.0} -> End");
		parser.addToNet(expected,     "p1 -> {c 1.0} -> End");
		StochasticNet net = estimateWithDefault(expected,
									"a b",
									"a c");
		assertTrue( StochasticPetriNetUtils.areEqual(expected, net) );
	}
	
	@Test
	public void unsound() {
		// This example comes from section 4.3, p1137 of 
		// van der Aalst et al - Workflow mining: Discovering Process Models From Event Logs (2004)
		// It shows the alpha class algo producing an unsound and non free-choice
		// workflow net
		StochasticNet expected = parser.createNet("expected", 
							     "Start -> {a 6.0} -> p1 -> {b 1.0} -> p2 -> {d 3.0} -> End");
		parser.addToNet(expected,"Start -> {a 6.0} -> p1 -> {e 1.0} -> p2");
		parser.addToNet(expected,"Start -> {a 6.0} -> p3 -> {c 1.0} -> p4 -> {d 3.0} -> End");
		parser.addToNet(expected,                    "p3 -> {e 1.0} -> p4");
		StochasticNet net = estimateWithDefault(expected,
									"a b c d","a c b d",
									"a e d");
		checkEqual( "unsound", expected, net);
	}
	
	@Test
	public void modelLargerThanLog() {
		StochasticNet mined = parser.createNet("mined", 
				"Start -> {a} -> p1 -> {b} -> p2 -> {c} -> End");
		StochasticNet expected = parser.createNet("expected", 
				"Start -> {a 1.0} -> p1 -> {b 4.0} -> p2 -> {c 2.0} -> End");
		StochasticNet net = estimate(mined,"b c",
										   "b c");
		checkEqual( "model larger", expected, net);
	}

	@Test
	public void runningExample1() {
		// not a great example as net is unsound
		StochasticNet mined = parser.createNet("expected", 
				  	"Start -> {a} -> p1 -> {c} -> p2 -> {d} -> End");
		parser.addToNet(mined, 		"p1 -> {tau} -> p2");
		parser.addToNet(mined,
					"Start -> {b} -> p3 -> {b} -> p3");
		parser.addToNet(mined,
									"p3 -> {d} -> End");
		StochasticNet net = estimate(mined,"a c d",
										   "a d","a d",
										   "b d",
										   "b b b d");
		// pair frequencies:		weights:
		// 	a c	1					a = 3 + 0 + (1) = 4  						
		// 	a d	2					b = 2 + 0 + 2 + 2 = 6 					
		// 	b b	2					c = 1						
		// 	b d	2					d = 4 						
		// 	c d	1					tau = 1
		StochasticNet expected = parser.createNet("expected",  
				  "Start -> {a 4.0} -> p1 -> {c} -> p2 -> {d 5.0} -> End");
		parser.addToNet(expected,     	  "p1 -> {tau} -> p2");
		parser.addToNet(expected,     	  
					"Start -> {b 6.0} -> p3 -> {b 6.0} -> p3");
		parser.addToNet(expected,
					"p3 -> {d 5.0} -> End");
		checkEqual("running example", expected, net);		
	}

	
}
