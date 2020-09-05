package au.edu.qut.pm.spn_discover;

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

import au.edu.qut.prom.helpers.PetriNetFragmentParser;
import au.edu.qut.prom.helpers.StochasticTestUtils;

public class MeanScaledActivityPairEstimatorTest {

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
		return StochasticTestUtils.estimateFromTraces(minedModel,new MeanScaledActivityPairRHEstimator(),traces);
	}
	
	private StochasticNet estimateWithDefault(StochasticNet expected, String ... traces) {
		return StochasticTestUtils.estimateWithDefault(expected,new MeanScaledActivityPairRHEstimator(),traces);
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
		// Note this matches twoSeq due to scaled estimator
		StochasticNet expected = parser.createNet("expected", 
				"Start -> {a 2.0} -> p1 -> {b} -> End");
		StochasticNet net = estimateWithDefault(expected,"a b",
														 "a b");
		checkEqual("two seq two events", expected, net);
	}
	
	@Test
	public void immediateChoice() {
		StochasticNet expected = parser.createNet("expected", 
				  					"Start -> {a} -> p1 -> {c} -> End");
		parser.addToNet(expected,   "Start -> {b} -> p2 -> {c} -> End");
		// a = 1 (start) + 1 (a c) + 0 (end)
		// b = 1 (start) + 1 (b c) + 0 (end)
		// c = 0 (start)           + 1 (end)
		// mean = 2.0
		StochasticNet net = estimateWithDefault(expected,"a b c",
														 "b a c");
		checkEqual("one choice", expected, net);
	}
	
	@Test
	public void oneInlineChoiceUnevenWeights() {
		StochasticNet expected = parser.createNet("expected", 
				  "Start -> {a 2.66666} -> p1 -> {b} -> p2 -> {d 1.33333} -> End");
		parser.addToNet(expected,         "p1 -> {c 0.33333} -> p2");
		StochasticNet net = estimateWithDefault(expected,
									"a b d","a b d","a b d",
									"a c d");
		// mean = 4 + 3 + 1 + 4 / 4 = 3
		checkEqual("choice uneven weights", expected, net);
	}


	@Test
	public void inlineChoice() {
		StochasticNet expected = parser.createNet("expected", 
				  "Start -> {a 2.66666} -> p1 -> {b 0.66666} -> p2 -> {d 1.33333} -> End");
		parser.addToNet(expected,         "p1 -> {c 0.66666} -> p2");
		StochasticNet net = estimateWithDefault(expected,
									"a b d",
									"a c d");
		// mean = 2 + 1 + 1 + 2 / 4 = 6/4 = 1.5
		checkEqual("inline choice", expected, net);
	}
	
	@Test
	public void threeLoop() {
		StochasticNet expected = parser.createNet("expected", 
				  "Start -> {a 2.28571} -> p1 -> {b 1.71428} -> End");
		parser.addToNet(expected,         "p1 -> {b 1.71428} -> p2 -> {c 0.571428} -> p3 -> {d 0.571428} -> p1");
		StochasticNet net = estimateWithDefault(expected,
									"a b",
									"a b c d b");
		// mean 2 + 3 + 1 + 1 / 4 = 1.75
		checkEqual("three loop", expected, net);
	}


	@Test
	public void choiceDivergentBeforeFinal() {
		StochasticNet expected = parser.createNet("expected", 
				  "Start -> {a 3.0} -> p1 -> {b 0.75} -> End");
		parser.addToNet(expected,     "p1 -> {c 0.75} -> End");
		StochasticNet net = estimateWithDefault(expected,
									"a b",
									"a c");
		// mean = 4/3
		checkEqual("choice divergent",expected, net );
	}

	@Test
	public void modelLargerThanLog() {
		StochasticNet mined = parser.createNet("mined", 
				"Start -> {a} -> p1 -> {b} -> p2 -> {c} -> End");
		// note the mean has to be calculated including zero frequencies
		// mean = 4/3
		StochasticNet expected = parser.createNet("expected", 
				"Start -> {a 1.0} -> p1 -> {b 3.0} -> p2 -> {c 1.5} -> End");
		StochasticNet net = estimate(mined,"b c",
										   "b c");
		checkEqual( "model larger", expected, net);
	}
	
}
