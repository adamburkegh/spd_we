package au.edu.qut.pm.spn_discover;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XLog;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.util.Pair;
import org.processmining.models.graphbased.directed.petrinet.StochasticNet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;

import au.edu.qut.pm.alpha.StochasticAlphaMiner;
import au.edu.qut.pm.alpha.StochasticAlphaMinerFactory;
import au.edu.qut.prom.helpers.ConsoleUIPluginContext;
import au.edu.qut.prom.helpers.HeadlessUIPluginContext;
import au.edu.qut.prom.helpers.PetriNetFragmentParser;
import au.edu.qut.prom.helpers.StochasticPetriNetUtils;
import au.edu.qut.prom.helpers.StochasticTestUtils;
import au.edu.qut.xes.helpers.DelimitedTraceToXESConverter;

public class StochasticAlphaMinerBillClintonTest {

	private static Logger LOGGER = LogManager.getLogger();
	
	private static final XEventNameClassifier NAME_CLASSIFIER = new XEventNameClassifier();  
	private static final PluginContext PLUGIN_CONTEXT = 
			new HeadlessUIPluginContext(new ConsoleUIPluginContext(), "sambctest");
	
	private PetriNetFragmentParser parser = new PetriNetFragmentParser();
	
	@BeforeClass
	public static void beforeClass() {
		StochasticTestUtils.initializeLogging();
	}
	
	@Before
	public void setUp() {
		parser = new PetriNetFragmentParser();
	}

	private static StochasticNet mineLog(String ... traces) {
		DelimitedTraceToXESConverter converter = new DelimitedTraceToXESConverter(); 
		XLog log = converter.convertTextArgs(traces);
		StochasticAlphaMiner<XEventClass> miner = 
				StochasticAlphaMinerFactory.createBillClintonSAM(NAME_CLASSIFIER, log, PLUGIN_CONTEXT);
		Pair<StochasticNet, Marking> result = miner.runStochasticMiner();
		return result.getFirst();		
	}

	private static void checkEqual(String message, StochasticNet expected, StochasticNet net) {
		StochasticTestUtils.checkEqual(LOGGER,message,expected,net);
	}
	
	@Test
	public void singleTransition() {
		StochasticNet expected = parser.createNet("expected", 
							"Start -> {a} -> End");
		StochasticTestUtils.renamePlacesByTransition(expected);
		StochasticNet net = mineLog("a");
		checkEqual( "single transition", expected, net);
		assertEquals(2,net.getEdges().size());
		assertEquals(2,net.getPlaces().size());
		Collection<Transition> transitions = net.getTransitions();
		assertEquals(1,transitions.size());
		Transition transition = transitions.iterator().next();
		assertEquals("a", transition.getLabel());
		assertEquals(1,transition.getGraph().getInEdges(transition).size());
		assertEquals(1,transition.getGraph().getOutEdges(transition).size());
		StochasticTestUtils.assertWeightsEqual(1.0d,transition);
	}

	@Test
	public void twoSequentialTransitions() {
		StochasticNet net = mineLog("a b");
		assertEquals(4,net.getEdges().size());
		assertEquals(3,net.getPlaces().size());
		Collection<Transition> transitions = net.getTransitions();
		assertEquals(2,transitions.size());
		StochasticNet expected = parser.createNet("expected", 
				"Start -> {a} -> p1 -> {b} -> End");
		StochasticTestUtils.renamePlacesByTransition(expected);
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
				"Start -> {a 2.0} -> p1 -> {b 2.0} -> End");
		StochasticNet net = mineLog("a b",
									"a b");
		StochasticTestUtils.renamePlacesByTransition(expected);
		checkEqual("two seq two events", expected, net);
		// Interesting property - pure frequency estimators given different weights 
		// (linearly related) to duplicated 
		// traces ... eg the same log repeated results in doubling all the weights
	}
	
	@Test
	public void immediateChoice() {
		StochasticNet expected = parser.createNet("expected", 
				  					"Start -> {a} -> p1 -> {c 2.0} -> End");
		parser.addToNet(expected,   "Start -> {b} -> p2 -> {c 2.0} -> End");
		// a = 1 (start) + 1 (a c) + 0 (end)
		// b = 1 (start) + 1 (b c) + 0 (end)
		// c = 0 (start)           + 1 (end)
		StochasticTestUtils.renamePlacesByTransition(expected);
		StochasticNet net = mineLog("a b c",
									"b a c");
		checkEqual("one choice", expected, net);
	}
	
	@Test
	public void oneInlineChoiceUnevenWeights() {
		StochasticNet expected = parser.createNet("expected", 
				  "Start -> {a 4.0} -> p1 -> {b 3.0} -> p2 -> {d 4.0} -> End");
		parser.addToNet(expected,     "p1 -> {c 1.0} -> p2");
		StochasticTestUtils.renamePlacesByTransition(expected);
		StochasticNet net = mineLog("a b d",
									"a b d",
									"a b d",
									"a c d");
		assertTrue( StochasticPetriNetUtils.areEqual(expected, net) );
	}


	@Test
	public void inlineChoice() {
		StochasticNet expected = parser.createNet("expected", 
				  "Start -> {a 2.0} -> p1 -> {b} -> p2 -> {d 2.0} -> End");
		parser.addToNet(expected,     "p1 -> {c} -> p2");
		StochasticTestUtils.renamePlacesByTransition(expected);
		StochasticNet net = mineLog("a b d",
									"a c d");
		assertTrue( StochasticPetriNetUtils.areEqual(expected, net) );
	}
	
	@Test
	public void threeLoop() {
		StochasticNet expected = parser.createNet("expected", 
				  "Start -> {a 2.0} -> p1 -> {b 3.0} -> End");
		parser.addToNet(expected,     "p1 -> {b 3.0} -> p2 -> {c} -> p3 -> {d} -> p1");
		StochasticTestUtils.renamePlacesByTransition(expected);
		StochasticNet net = mineLog("a b",
									"a b c d b");
		checkEqual("three loop", expected, net);
	}


	@Test
	public void choiceDivergentBeforeFinal() {
		StochasticNet expected = parser.createNet("expected", 
				  "Start -> {a 2.0} -> p1 -> {b 1.0} -> End");
		parser.addToNet(expected,     "p1 -> {c 1.0} -> End");
		StochasticTestUtils.renamePlacesByTransition(expected);
		StochasticNet net = mineLog("a b",
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
							     "Start -> {a 3.0} -> p1 -> {b 1.33333} -> p2 -> {d 4.0} -> End");
		parser.addToNet(expected,"Start -> {a 3.0} -> p1 -> {e 1.33333} -> p2");
		parser.addToNet(expected,"Start -> {a 3.0} -> p3 -> {c 1.33333} -> p4 -> {d 4.0} -> End");
		parser.addToNet(expected,                    "p3 -> {e 1.33333} -> p4");
		StochasticTestUtils.renamePlacesByTransitionLabelSorted(expected);
		StochasticNet net = mineLog("a b c d",
									"a c b d",
									"a e d");
		checkEqual( "unsound", expected, net);
	}

	@Test
	public void divergeConvergeUneven() {
		StochasticNet expected = parser.createNet("expected", 
							     "Start -> {a 3.0} -> p1 -> {d 0.375} -> End");
		parser.addToNet(expected,"Start -> {a 3.0} -> p1 -> {e 0.375} -> End");
		parser.addToNet(expected,"Start -> {a 3.0} -> p1 -> {f 13.583333} -> End");
		parser.addToNet(expected,"Start -> {a 3.0} -> p2 -> {f 13.583333} -> End");
		parser.addToNet(expected,"Start -> {b 2.0} -> p2 -> {f 13.583333} -> End");
		parser.addToNet(expected,"Start -> {b 2.0} -> p3 -> {f 13.583333} -> End");
		parser.addToNet(expected,"Start -> {c 6.0} -> p2 -> {f 13.583333} -> End");
		parser.addToNet(expected,"Start -> {c 6.0} -> p3 -> {g 2.666666} -> End");
		StochasticTestUtils.renamePlacesByTransitionLabelSorted(expected);
		StochasticNet net = mineLog("a d",
									"a e",
									"a f",
									"b f",
									"b g",
									"c f","c f","c f","c f",
									"c g","c g");
		// p1 = 3			= 3
		// p2 = 1 + 1 + 4	= 6
		// p3 = 2 + 1 + 1	= 4
		// f  = 3*6/8 + 6*6/6 + 8*6/9 
		checkEqual( "diverge converge uneven", expected, net);
	}

	
}
