package au.edu.qut.pm.spn_estimator;

import static org.junit.Assert.assertEquals;

import java.util.Collection;

import au.edu.qut.pm.util.LogManager;
import au.edu.qut.pm.util.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.processmining.models.graphbased.directed.petrinet.StochasticNet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

import au.edu.qut.prom.helpers.PetriNetFragmentParser;
import au.edu.qut.prom.helpers.StochasticTestUtils;

public class BillClintonTest {

	private static Logger LOGGER = LogManager.getLogger();
		
	private PetriNetFragmentParser parser = new PetriNetFragmentParser();
	
	private static StochasticNet estimate(StochasticNet minedModel, String ... traces) {
		return StochasticTestUtils.estimateFromTraces(minedModel,new BillClintonEstimator(),traces);
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
		StochasticNet mined = parser.createNet("mined", 
				"Start -> {a} -> End");
		StochasticNet expected = parser.createNet("expected", 
							"Start -> {a} -> End");
		StochasticNet net = estimate(mined,"a");
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
		StochasticNet mined = parser.createNet("mined", 
				"Start -> {a} -> p1 -> {b} -> End");
		StochasticNet expected = parser.createNet("expected", 
				"Start -> {a} -> p1 -> {b} -> End");
		StochasticNet net = estimate(mined,"a b");
		checkEqual( "two sequential", expected, net);
	}


	@Test
	public void modelLargerThanLog() {
		StochasticNet mined = parser.createNet("mined", 
				"Start -> {a} -> p1 -> {b} -> End");
		StochasticNet expected = parser.createNet("expected", 
				"Start -> {a} -> p1 -> {b} -> End");
		StochasticNet net = estimate(mined,"a");
		checkEqual( "model larger", expected, net);
	}

	@Test
	public void silentTransitions() {
		StochasticNet mined = parser.createNet("mined", 
				"Start -> {a} -> p1 -> {tau} -> p2 -> {b} -> End");
		StochasticNet expected = parser.createNet("expected", 
				"Start -> {a 3.0} -> p1 -> {tau} -> p2 -> {b} -> End");
		StochasticNet net = estimate(mined,"a b", "a b", "a b");
		checkEqual( "model larger", expected, net);
	}
	

	@Test
	public void twoSequentialTransitionsTwoEvents() {
		StochasticNet expected = parser.createNet("expected", 
				"Start -> {a 2.0} -> p1 -> {b 2.0} -> End");
		StochasticNet mined = StochasticTestUtils.resetWeightCopy(expected);
		StochasticNet net = estimate(mined,"a b","a b");
		checkEqual( "two seq two events", expected, net);
	}
	
	@Test
	public void immediateChoice() {
		StochasticNet expected = parser.createNet("expected", 
				  					"Start -> {a} -> p1 -> {c 2.0} -> End");
		parser.addToNet(expected,   "Start -> {b} -> p2 -> {c 2.0} -> End");
		// a = 1 (start) + 1 (a c) + 0 (end)
		// b = 1 (start) + 1 (b c) + 0 (end)
		// c = 0 (start)           + 1 (end)
		StochasticNet mined = StochasticTestUtils.resetWeightCopy(expected);
		StochasticNet net = estimate(mined,"a b c",
									 	   "b a c");
		checkEqual("one choice", expected, net);
	}
	
	@Test
	public void oneInlineChoiceUnevenWeights() {
		StochasticNet expected = parser.createNet("expected", 
				  "Start -> {a 4.0} -> p1 -> {b 3.0} -> p2 -> {d 4.0} -> End");
		parser.addToNet(expected,     "p1 -> {c 1.0} -> p2");
		StochasticNet mined = StochasticTestUtils.resetWeightCopy(expected);
		StochasticNet net = estimate(mined,"a b d","a b d","a b d",
										   "a c d");
		checkEqual("inline uneven", expected, net);
	}


	@Test
	public void inlineChoice() {
		StochasticNet expected = parser.createNet("expected", 
				  "Start -> {a 2.0} -> p1 -> {b} -> p2 -> {d 2.0} -> End");
		parser.addToNet(expected,     "p1 -> {c} -> p2");
		StochasticNet mined = StochasticTestUtils.resetWeightCopy(expected);
		StochasticNet net = estimate(mined,"a b d",
										   "a c d");
		checkEqual("inline", expected, net);
	}
	
	@Test
	public void threeLoop() {
		StochasticNet expected = parser.createNet("expected", 
				  "Start -> {a 2.0} -> p1 -> {b 3.0} -> End");
		parser.addToNet(expected,     "p1 -> {b 3.0} -> p2 -> {c} -> p3 -> {d} -> p1");
		StochasticNet mined = StochasticTestUtils.resetWeightCopy(expected);
		StochasticNet net = estimate(mined,"a b",
										   "a b c d b");
		checkEqual("three loop", expected, net);
	}

	@Test
	public void choiceDivergentBeforeFinal() {
		StochasticNet expected = parser.createNet("expected", 
				  "Start -> {a 2.0} -> p1 -> {b 1.0} -> End");
		parser.addToNet(expected,     "p1 -> {c 1.0} -> End");
		StochasticNet mined = StochasticTestUtils.resetWeightCopy(expected);
		StochasticNet net = estimate(mined,"a b",
										   "a c");
		checkEqual("choice divergent", expected, net);
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
		StochasticNet mined = StochasticTestUtils.resetWeightCopy(expected);
		StochasticNet net = estimate(mined,"a b c d",
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
		StochasticNet mined = StochasticTestUtils.resetWeightCopy(expected);
		StochasticNet net = estimate(mined,"a d",
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
		// pair frequencies:			activity frequencies
		// 	a c	1						a	3
		// 	a d	2						b	4
		// 	b b	2						c	1
		// 	b d	2						d	5
		// 	c d	1
		// p1 = 1 + 0 	= 1				a   = 5*3/7
		// p2 = 1 		= 1				b   = 5*4/7 + 4*4/9 = 20/7 + 16/9 = 292/63 
		// p3 = 2 + 2	= 4				c   = 1*1/2 = 1 
		// Start = 5					d   = 1*5/5 + 4*5/9 = 29/9
		//								tau = 1/2
		StochasticNet expected = parser.createNet("expected",  
				  "Start -> {a 2.14285} -> p1 -> {c 0.5} -> p2 -> {d 3.222222} -> End");
		parser.addToNet(expected,     	  "p1 -> {tau 0.5} -> p2");
		parser.addToNet(expected,     	  
					"Start -> {b 4.63492} -> p3 -> {b 4.63492} -> p3");
		parser.addToNet(expected,
					"p3 -> {d 3.2222222} -> End");
		checkEqual("running example", expected, net);		
	}
	
}
