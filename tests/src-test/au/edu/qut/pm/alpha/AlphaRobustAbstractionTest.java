package au.edu.qut.pm.alpha;

import static org.junit.Assert.*;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XLog;
import org.junit.Test;
import org.processmining.alphaminer.abstractions.AlphaRobustAbstraction;
import org.processmining.alphaminer.parameters.AlphaRobustMinerParameters;
import org.processmining.logabstractions.models.ColumnAbstraction;

import au.edu.qut.xes.helpers.DelimitedTraceToXESConverter;

public class AlphaRobustAbstractionTest {

	private static double EPSILON = 0.00001;
	
	private static XLog logFromTraces(String ... traces) {
		DelimitedTraceToXESConverter converter = new DelimitedTraceToXESConverter(); 
		XLog log = converter.convertTextArgs(traces);
		return log;
	}
	
	private void checkEqual(double a, double b) {
		assertTrue(String.format("%f - %f > %f ", a,b,EPSILON ), a - b < EPSILON);
	}

	private AlphaRobustAbstraction<XEventClass> abstractionFromTraces(String... traces) {
		XLog log = logFromTraces(traces);
		AlphaRobustMinerParameters parameters = new AlphaRobustMinerParameters(0.0,0.0,0.0);
		XEventClassifier classifier = new XEventNameClassifier();
		AlphaRobustAbstraction<XEventClass> abstraction = 
				AlphaAbstractionFactoryWithFixes.createAlphaRobustAbstraction(log, classifier, parameters);
		return abstraction;
	}

	
	@Test
	public void oneTraceOneEvent() {
		AlphaRobustAbstraction<XEventClass> abstraction = abstractionFromTraces("a");
		ColumnAbstraction<XEventClass> ac = abstraction.getRobustActivityCount();
		assertEquals(1,ac.getEventClasses().length);
		checkEqual(1.0, ac.getValue(0) );
	}


	@Test
	public void oneTraceTwoEvents() {
		AlphaRobustAbstraction<XEventClass> abstraction = abstractionFromTraces("a b");
		ColumnAbstraction<XEventClass> ac = abstraction.getRobustActivityCount();
		assertEquals(2,ac.getEventClasses().length);
		checkEqual(1.0, ac.getValue(0) );
		checkEqual(1.0, ac.getValue(1) );
	}

	
	@Test
	public void twoTraces() {
		AlphaRobustAbstraction<XEventClass> abstraction = abstractionFromTraces("a","a");
		ColumnAbstraction<XEventClass> ac = abstraction.getRobustActivityCount();
		assertEquals(1,ac.getEventClasses().length);
		checkEqual(2.0, ac.getValue(0) );
	}

	
}
