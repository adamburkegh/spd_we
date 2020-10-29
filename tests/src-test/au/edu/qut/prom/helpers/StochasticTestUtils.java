package au.edu.qut.prom.helpers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.StochasticNet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.TimedTransition;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

import au.edu.qut.pm.spn_estimator.LogSourcedWeightEstimator;
import au.edu.qut.pm.stochastic.StochasticNetCloner;
import au.edu.qut.xes.helpers.DelimitedTraceToXESConverter;

public class StochasticTestUtils {
	
	private static final double EPSILON = 0.0001d;
	private static final XEventNameClassifier NAME_CLASSIFIER = new XEventNameClassifier();
	

	public static void initializeLogging() {
		// ugh TODO log4j 2 warns against the volatility of this API
		// but we're not using maven and we don't have a reliable path
		LoggerContext context = (LoggerContext) LogManager.getContext(false);
		Configuration config = context.getConfiguration();
		LoggerConfig rootConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
		rootConfig.setLevel(Level.DEBUG);
		context.updateLoggers();
	}

	public static void renamePlacesByTransition(StochasticNet expected) {
		for (Place place: expected.getPlaces()) {
			StringBuffer newLabel = new StringBuffer("");
			newLabel.append("(");
			Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> inEdges = 
					expected.getInEdges(place);
			Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> outEdges = 
					expected.getOutEdges(place);
			if (inEdges.isEmpty() || outEdges.isEmpty()) {
				continue;
			}
			newLabel.append( 
					inEdges.stream().map(e -> e.getSource().getLabel() ).collect(Collectors.toList()) );
			newLabel.append(",");
			newLabel.append( 
					outEdges.stream().map(e -> e.getTarget().getLabel() ).collect(Collectors.toList()) );
			newLabel.append(")");
			place.getAttributeMap().put(AttributeMap.LABEL,newLabel.toString());
		}
	}

	public static void renamePlacesByTransitionLabelSorted(StochasticNet expected) {
		for (Place place: expected.getPlaces()) {
			StringBuffer newLabel = new StringBuffer("");
			newLabel.append("(");
			Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> inEdges = 
					expected.getInEdges(place);
			Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> outEdges = 
					expected.getOutEdges(place);
			if (inEdges.isEmpty() || outEdges.isEmpty()) {
				continue;
			}
			newLabel.append( 
					inEdges.stream().map(
							e -> e.getSource().getLabel() ).sorted().collect(Collectors.toList()) 
						);
			newLabel.append(",");
			newLabel.append( 
					outEdges.stream().map(
							e -> e.getTarget().getLabel() ).sorted().collect(Collectors.toList()) );
			newLabel.append(")");
			place.getAttributeMap().put(AttributeMap.LABEL,newLabel.toString());
		}
	}
	
	public static String formatDebugNet(StochasticNet net) {
		StringBuffer output = new StringBuffer();
		final String NEWLINE = "\n";
		output.append(net.getLabel());
		output.append(NEWLINE);
		output.append("Places: {");
		for (Place place: net.getPlaces()) {
			output.append(place.getLabel());
			output.append("  ");
		}
		output.append("}");
		output.append(NEWLINE);
		output.append("Transitions: {");
		for (Transition transition: net.getTransitions()) {
			output.append(transition.getLabel());
			output.append("_");
			output.append(((TimedTransition)transition).getWeight());
			output.append("  ");
		}
		output.append("}");
		return output.toString();
	}

	public static void debug(StochasticNet net, Logger logger) {
		logger.debug(formatDebugNet(net));
	}

	public static void assertWeightsEqual( double expected, TimedTransition transition) {
		assertEquals("For transition: " + transition.getLabel() + ",",
				expected, transition.getWeight(),EPSILON);
	}

	/** Convenience method because everything we're testing is a TimedTransition coming out of a 
	 *Transition collection
	 */
	public static void assertWeightsEqual( double expected, Transition transition) {
		assertWeightsEqual(expected,(TimedTransition)transition);
	}

	public static void checkEqual(Logger logger, String message, StochasticNet expected, StochasticNet net) {
		boolean result = StochasticPetriNetUtils.areEqual(expected, net);
		if (!result) {
			logger.debug("Checking equality for " + message);
			debug(expected, logger);
			debug(net, logger);
		}
		assertTrue(message,result);		
	}

	public static StochasticNet resetWeightCopy(StochasticNet net) {
		StochasticNet result = StochasticNetCloner.cloneFromPetriNet(net);
		for (Transition tranT: result.getTransitions()) {
			TimedTransition tran = (TimedTransition)tranT;
			tran.setWeight(1.0);
		}
		return result;
	}

	public static StochasticNet estimateFromTraces(StochasticNet minedModel, LogSourcedWeightEstimator estimator, 
			String ... traces) 
	{
		DelimitedTraceToXESConverter converter = new DelimitedTraceToXESConverter();
		XLog log = converter.convertTextArgs(traces);
		PetriNetFragmentParser eParser = new PetriNetFragmentParser();
		AcceptingPetriNet apn = eParser.markInitialFinalPlaces(minedModel);
		StochasticNet result = estimator.estimateWeights(apn, log, NAME_CLASSIFIER);
		return result;		
	}

	public static StochasticNet estimateWithDefault(StochasticNet expected, LogSourcedWeightEstimator estimator,
			String ... traces) {
		DelimitedTraceToXESConverter converter = new DelimitedTraceToXESConverter();
		XLog log = converter.convertTextArgs(traces);
		StochasticNet mined = StochasticTestUtils.resetWeightCopy(expected);
		PetriNetFragmentParser eParser = new PetriNetFragmentParser();
		AcceptingPetriNet apn = eParser.markInitialFinalPlaces(mined);
		StochasticNet result = 
				estimator.estimateWeights(apn, log, NAME_CLASSIFIER);
		return result;
	}
	
}
