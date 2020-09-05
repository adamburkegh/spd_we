package au.edu.qut.pm.spn_conformance;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.earthmoversstochasticconformancechecking.parameters.EMSCParametersDefault;
import org.processmining.earthmoversstochasticconformancechecking.parameters.EMSCParametersLogModel;
import org.processmining.earthmoversstochasticconformancechecking.parameters.EMSCParametersLogModelAbstract;
import org.processmining.earthmoversstochasticconformancechecking.plugins.EarthMoversStochasticConformancePlugin;
import org.processmining.earthmoversstochasticconformancechecking.tracealignments.StochasticTraceAlignmentsLogModel;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.models.graphbased.directed.petrinet.StochasticNet;
import org.processmining.models.semantics.petrinet.Marking;

import au.edu.qut.pm.spn_discover.Measure;
import au.edu.qut.pm.spn_discover.SPNQualityCalculator;
import au.edu.qut.pm.spn_discover.TaskStats;

public class EarthMoversCalculator implements SPNQualityCalculator {

	private class EMSCParametersLogModelNoAlignments extends EMSCParametersLogModelAbstract {

		public EMSCParametersLogModelNoAlignments(XEventClassifier classifier) {
			super(EMSCParametersDefault.defaultDistanceMatrix, 
					classifier,
					EMSCParametersDefault.defaultTerminationStrategy, 
					EMSCParametersDefault.defaultDebug, 
					false);
		}

	}
	
	private static Logger LOGGER = LogManager.getLogger();

	@Override
	public String getReadableId() {
		return "Earth Movers Similarity";
	}

	@Override
	public void calculate(PluginContext context, StochasticNet net, XLog log, 
			XEventClassifier classifier, TaskStats stats) throws Exception 
	{
		LOGGER.info("Computing earth-movers' distance (SL) ");
		EMSCParametersLogModel parameters = new EMSCParametersLogModelNoAlignments(classifier);
		
		Marking initialMarking = EarthMoversStochasticConformancePlugin.getInitialMarking(net);
		LOGGER.debug("Initial marking {}",initialMarking);
		StochasticTraceAlignmentsLogModel stAlign = EarthMoversStochasticConformancePlugin.measureLogModel(log, net,
				initialMarking, parameters, new ProMCanceller() {
					public boolean isCancelled() {
						return context.getProgress().isCancelled();
					}
				});
		stats.setMeasure(Measure.EARTH_MOVERS_SIMILARITY, stAlign.getSimilarity()); 
	}

}
