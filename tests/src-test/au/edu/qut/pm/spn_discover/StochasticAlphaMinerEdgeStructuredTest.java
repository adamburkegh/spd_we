package au.edu.qut.pm.spn_discover;

import static org.junit.Assert.*;

import au.edu.qut.pm.util.LogManager;
import au.edu.qut.pm.util.Logger;
import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XLog;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.util.Pair;
import org.processmining.models.graphbased.directed.petrinet.StochasticNet;
import org.processmining.models.semantics.petrinet.Marking;

import au.edu.qut.pm.alpha.StochasticAlphaMiner;
import au.edu.qut.pm.alpha.StochasticAlphaMinerFactory;
import au.edu.qut.prom.helpers.ConsoleUIPluginContext;
import au.edu.qut.prom.helpers.HeadlessUIPluginContext;
import au.edu.qut.prom.helpers.PetriNetFragmentParser;
import au.edu.qut.prom.helpers.StochasticPetriNetUtils;
import au.edu.qut.prom.helpers.StochasticTestUtils;
import au.edu.qut.xes.helpers.DelimitedTraceToXESConverter;

public class StochasticAlphaMinerEdgeStructuredTest {
	
	private static Logger LOGGER = LogManager.getLogger();

	private static final XEventNameClassifier NAME_CLASSIFIER = new XEventNameClassifier();  
	private static final PluginContext PLUGIN_CONTEXT = 
			new HeadlessUIPluginContext(new ConsoleUIPluginContext(), "samestest");
	
	private PetriNetFragmentParser parser = new PetriNetFragmentParser();
	
	private static StochasticNet mineLog(String ... traces) {
		DelimitedTraceToXESConverter converter = new DelimitedTraceToXESConverter(); 
		XLog log = converter.convertTextArgs(traces);
		StochasticAlphaMiner<XEventClass> miner = 
				StochasticAlphaMinerFactory.createEdgeStructuredSAM(NAME_CLASSIFIER, log, PLUGIN_CONTEXT);
		Pair<StochasticNet, Marking> result = miner.runStochasticMiner();
		return result.getFirst();		
	}

	@BeforeClass
	public static void beforeClass() {
		StochasticTestUtils.initializeLogging();
	}
	
	@Before
	public void setUp() {
		parser = new PetriNetFragmentParser();
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
		assertTrue( StochasticPetriNetUtils.areEqual(expected, net) );
	}
	
	@Test
	public void twoSequentialTransitions() {
		StochasticNet expected = parser.createNet("expected", 
				"Start -> {a} -> p1 -> {b} -> End");
		StochasticTestUtils.renamePlacesByTransition(expected);
		StochasticNet net = mineLog("a b");
		checkEqual("two seq", expected, net);
	}
	
	@Test
	public void immediateChoice() {
		StochasticNet expected = parser.createNet("expected", 
				  					"Start -> {a 0.5} -> p1 -> {c 1.0} -> End");
		parser.addToNet(expected,   "Start -> {b 0.5} -> p2 -> {c 1.0} -> End");
		StochasticTestUtils.renamePlacesByTransition(expected);
		StochasticNet net = mineLog("a b c",
									"b a c");
		checkEqual("one choice", expected, net);
	}
	
	@Test
	public void oneInlineChoiceUnevenWeights() {
		StochasticNet expected = parser.createNet("expected", 
				  "Start -> {a 1.0} -> p1 -> {b 0.75} -> p2 -> {d 1.0} -> End");
		parser.addToNet(expected,     "p1 -> {c 0.25} -> p2");
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
				  "Start -> {a 1.0} -> p1 -> {b 0.5} -> p2 -> {d 1.0} -> End");
		parser.addToNet(expected,     "p1 -> {c 0.5} -> p2");
		StochasticTestUtils.renamePlacesByTransition(expected);
		StochasticNet net = mineLog("a b d",
									"a c d");
		assertTrue( StochasticPetriNetUtils.areEqual(expected, net) );
	}
	
	
	@Test
	public void selfLoop() {
		StochasticNet expected = parser.createNet("expected", 
				  "Start -> {a 1.0} -> p1 -> {b 0.5} -> p2 -> {d 1.0} -> End");
		parser.addToNet(expected,     "p1 -> {b 0.5} -> p3 -> {b 0.5} -> p3");
		StochasticTestUtils.renamePlacesByTransition(expected);
		StochasticNet net = mineLog("a b d",
									"a b b d");
		// known alpha classic limitation on self-loops
		assertFalse(StochasticPetriNetUtils.areEqual(expected, net));
	}
	
	@Test
	public void twoLoop() {
		StochasticNet expected = parser.createNet("expected", 
				  "Start -> {a 1.0} -> p1 -> {b 0.5} -> End");
		parser.addToNet(expected,     "p1 -> {b 0.5} -> p2 -> {c 0.5} -> p3 -> {b 0.5} -> End");
		StochasticTestUtils.renamePlacesByTransition(expected);
		StochasticNet net = mineLog("a b",
									"a b c b");
		// known alpha classic limitation on two-loops
		assertFalse(StochasticPetriNetUtils.areEqual(expected, net));
	}

	@Test
	public void threeLoop() {
		StochasticNet expected = parser.createNet("expected", 
				  "Start -> {a} -> p1 -> {b} -> p2 -> {c 0.6} -> End");
		parser.addToNet(expected, "p1 -> {b} -> p2 -> {d 0.4} -> p3 -> {e} -> p1");
		// Note that the loop adds extra weight to the exiting transition on a pure frequency basis
		// there are three c vs two e even though the choice is between c and e
		StochasticTestUtils.debug(expected, LOGGER);
		StochasticTestUtils.renamePlacesByTransition(expected);
		StochasticNet net = mineLog("a b c",
									"a b d e b c",
									"a b d e b c");
		checkEqual("three loop", expected, net);
	}

	@Test
	public void threeLoopAlphabetic() {
		// A quirk of alpha miner or the underlying data structure sorts the nodes alphabetically
		// by label
		StochasticNet expected = parser.createNet("expected", 
				  "Start -> {z} -> p1 -> {y} -> p2 -> {x 0.6} -> End");
		parser.addToNet(expected, "p1 -> {y} -> p2 -> {w 0.4} -> p3 -> {v} -> p1");
		StochasticTestUtils.debug(expected, LOGGER);
		// ... so we rename the net accordingly 
		StochasticTestUtils.renamePlacesByTransitionLabelSorted(expected);
		StochasticNet net = mineLog("z y x",
									"z y w v y x",
									"z y w v y x");
		checkEqual("three loop", expected, net);
	}
	
	@Test
	public void choiceOrTerminate() {
		StochasticNet expected = parser.createNet("expected", 
				  "Start -> {a} -> p1 -> {b} -> End");
		parser.addToNet(expected, "p1 -> {b} -> p2 -> {c} -> End");
		StochasticTestUtils.debug(expected, LOGGER);
		StochasticTestUtils.renamePlacesByTransition(expected);
		StochasticNet net = mineLog("a b",
									"a b c");
		// This unsafe and therefore unsound net actually always produces abc
		// Note that b -> End and b -> produces two tokens
		// Then the token at b has to transition through c to terminate
		// So marking this expected behaviour. At time of writing 
		// it seems this estimator may not be that useful anyway - the ratio
		// is unnecessary and it's probably equivalent to basic activity frequency
		checkEqual("choiceOrTerminate", expected, net);
	}
	
	@Test
	public void choiceDivergentBeforeFinal() {
		StochasticNet expected = parser.createNet("expected", 
				  "Start -> {a 1.0} -> p1 -> {b 0.5} -> End");
		parser.addToNet(expected,     "p1 -> {c 0.5} -> End");
		StochasticTestUtils.renamePlacesByTransition(expected);
		StochasticNet net = mineLog("a b",
									"a c");
		assertTrue( StochasticPetriNetUtils.areEqual(expected, net) );
	}
	

}
