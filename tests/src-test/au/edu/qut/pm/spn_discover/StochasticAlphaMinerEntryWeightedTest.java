package au.edu.qut.pm.spn_discover;

import static org.junit.Assert.assertTrue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XLog;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.petrinet.StochasticNet;

import au.edu.qut.pm.stochastic.StochasticNetDescriptor;
import au.edu.qut.prom.helpers.ConsoleUIPluginContext;
import au.edu.qut.prom.helpers.HeadlessUIPluginContext;
import au.edu.qut.prom.helpers.PetriNetFragmentParser;
import au.edu.qut.prom.helpers.StochasticPetriNetUtils;
import au.edu.qut.prom.helpers.StochasticTestUtils;
import au.edu.qut.xes.helpers.DelimitedTraceToXESConverter;

public class StochasticAlphaMinerEntryWeightedTest {

	private static Logger LOGGER = LogManager.getLogger();
	
	private static final XEventNameClassifier NAME_CLASSIFIER = new XEventNameClassifier();  
	private static final PluginContext PLUGIN_CONTEXT = 
			new HeadlessUIPluginContext(new ConsoleUIPluginContext(), "samfreqtest");
	
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
		StochalphaEntryWeightedMiner miner = new StochalphaEntryWeightedMiner();
		StochasticNetDescriptor result = miner.runMiner(PLUGIN_CONTEXT, log, NAME_CLASSIFIER);
		return result.getNet();		
	}
	
	private static void checkEqual(String message, StochasticNet expected, StochasticNet net) {
		StochasticTestUtils.checkEqual(LOGGER,message,expected,net);
	}

	
	@Test
	public void threeLoopAlphabetic() {
		// A quirk of alpha miner or the underlying data structure sorts the nodes alphabetically
		// by label
		StochasticNet expected = parser.createNet("expected", 
				  "Start -> {z 3.0} -> p1 -> {y 5.0} -> p2 -> {x 3.0} -> End");
		parser.addToNet(expected,     "p1 -> {y 5.0} -> p2 -> {w 2.0} -> p3 -> {v 2.0} -> p1");
		// ... so we rename the net accordingly 
		StochasticTestUtils.renamePlacesByTransitionLabelSorted(expected);
		StochasticNet net = mineLog("z y x",
									"z y w v y x",
									"z y w v y x");
		checkEqual("three loop", expected, net);
	}
	
	@Test
	public void choiceOrTerminate() {
		// Note this is not a sound workflow net
		StochasticNet expected = parser.createNet("expected", 
				  "Start -> {a 2.0} -> p1 -> {b 2.0} -> End");
		parser.addToNet(expected,     "p1 -> {b 2.0} -> p2 -> {c 2.0} -> End");
		// a = 1 * 2 * 1
		// b = 1 * 2 * 1
		// c = 1 * 1 * 2
		StochasticTestUtils.renamePlacesByTransition(expected);
		StochasticNet net = mineLog("a b",
									"a b c");
		checkEqual("choiceOrTerminate", expected, net);
	}
	
	@Test
	public void choiceDivergentBeforeFinal() {
		StochasticNet expected = parser.createNet("expected", 
				  "Start -> {a 2.0} -> p1 -> {b} -> End");
		parser.addToNet(expected,     "p1 -> {c} -> End");
		StochasticTestUtils.renamePlacesByTransition(expected);
		StochasticNet net = mineLog("a b",
									"a c");
		assertTrue( StochasticPetriNetUtils.areEqual(expected, net) );
	}

	@Test
	public void singleTransition() {
		StochasticNet expected = parser.createNet("expected", 
							"Start -> {a} -> End");
		StochasticTestUtils.renamePlacesByTransition(expected);
		StochasticNet net = mineLog("a");
		checkEqual( "single transition", expected, net);
	}

	@Test
	public void unsound() {
		// This example comes from section 4.3, p1137 of 
		// van der Aalst et al - Workflow mining: Discovering Process Models From Event Logs (2004)
		// It shows the alpha class algo producing an unsound and non free-choice
		// workflow net
		StochasticNet expected = parser.createNet("expected", 
							     "Start -> {a 3.0} -> p1 -> {b 2.0} -> p2 -> {d 12.0} -> End");
		parser.addToNet(expected,"Start -> {a 3.0} -> p1 -> {e 4.0} -> p2");
		parser.addToNet(expected,"Start -> {a 3.0} -> p3 -> {c 2.0} -> p4 -> {d 12.0} -> End");
		parser.addToNet(expected,                    "p3 -> {e 4.0} -> p4");
		// All the place entry ratios are 1.0 ...
		StochasticTestUtils.renamePlacesByTransitionLabelSorted(expected);
		StochasticNet net = mineLog("a b c d",
									"a c b d",
									"a e d");
		checkEqual( "unsound", expected, net);
	}
	
	
	@Test
	public void immediateChoice() {
		StochasticNet expected = parser.createNet("expected", 
				  					"Start -> {a 2.0} -> p1 -> {c 8.0} -> End");
		parser.addToNet(expected,   "Start -> {b 2.0} -> p2 -> {c 8.0} -> End");
		// Note big weight on c, with two predecessor places
		StochasticTestUtils.renamePlacesByTransition(expected);
		StochasticNet net = mineLog("a b c",
									"b a c");
		checkEqual("one choice", expected, net);
	}
}
