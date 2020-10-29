package au.edu.qut.pm.spn_estimator;

import static org.junit.Assert.assertTrue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.processmining.models.graphbased.directed.petrinet.StochasticNet;

import au.edu.qut.prom.helpers.PetriNetFragmentParser;
import au.edu.qut.prom.helpers.StochasticPetriNetUtils;
import au.edu.qut.prom.helpers.StochasticTestUtils;

public class FrequencyEstimatorTest {

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
		return StochasticTestUtils.estimateFromTraces(minedModel,new FrequencyEstimator(),traces);
	}
	
	private static void checkEqual(String message, StochasticNet expected, StochasticNet net) {
		StochasticTestUtils.checkEqual(LOGGER,message,expected,net);
	}

	
	@Test
	public void threeLoopAlphabetic() {
		StochasticNet expected = parser.createNet("expected", 
				  "Start -> {z 3.0} -> p1 -> {y 5.0} -> p2 -> {x 3.0} -> End");
		parser.addToNet(expected,     "p1 -> {y 5.0} -> p2 -> {w 2.0} -> p3 -> {v 2.0} -> p1");
		StochasticNet mined = StochasticTestUtils.resetWeightCopy(expected);
		StochasticNet net = estimate(mined,
									"z y x",
									"z y w v y x",
									"z y w v y x");
		checkEqual("three loop", expected, net);
	}
	
	@Test
	public void choiceOrTerminate() {
		// Note this is not a sound workflow net
		StochasticNet expected = parser.createNet("expected", 
				  "Start -> {a 2.0} -> p1 -> {b 2.0} -> End");
		parser.addToNet(expected,     "p1 -> {b 2.0} -> p2 -> {c} -> End");
		StochasticNet mined = StochasticTestUtils.resetWeightCopy(expected);
		StochasticNet net = estimate(mined,"a b",
										   "a b c");
		checkEqual("choiceOrTerminate", expected, net);
	}
	
	@Test
	public void choiceDivergentBeforeFinal() {
		StochasticNet expected = parser.createNet("expected", 
				  "Start -> {a 2.0} -> p1 -> {b} -> End");
		parser.addToNet(expected,     "p1 -> {c} -> End");
		StochasticNet mined = StochasticTestUtils.resetWeightCopy(expected);
		StochasticNet net = estimate(mined,"a b",
										   "a c");
		assertTrue( StochasticPetriNetUtils.areEqual(expected, net) );
	}

	@Test
	public void singleTransition() {
		StochasticNet expected = parser.createNet("expected", 
							"Start -> {a} -> End");
		StochasticNet mined = StochasticTestUtils.resetWeightCopy(expected);
		StochasticNet net = estimate(mined,"a");
		checkEqual( "single transition", expected, net);
	}

	@Test
	public void unsound() {
		// This example comes from section 4.3, p1137 of 
		// van der Aalst et al - Workflow mining: Discovering Process Models From Event Logs (2004)
		// It shows the alpha class algo producing an unsound and non free-choice
		// workflow net
		StochasticNet expected = parser.createNet("expected", 
							     "Start -> {a 3.0} -> p1 -> {b 2.0} -> p2 -> {d 3.0} -> End");
		parser.addToNet(expected,"Start -> {a 3.0} -> p1 -> {e 1.0} -> p2");
		parser.addToNet(expected,"Start -> {a 3.0} -> p3 -> {c 2.0} -> p4 -> {d 3.0} -> End");
		parser.addToNet(expected,                    "p3 -> {e 1.0} -> p4");
		StochasticNet mined = StochasticTestUtils.resetWeightCopy(expected);
		StochasticNet net = estimate(mined,
									"a b c d",
									"a c b d",
									"a e d");
		checkEqual( "unsound", expected, net);
	}
	
}
