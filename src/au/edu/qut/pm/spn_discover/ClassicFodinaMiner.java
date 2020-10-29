package au.edu.qut.pm.spn_discover;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.StochasticNet;
import org.processmining.models.graphbased.directed.petrinet.elements.TimedTransition;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.AbstractResetInhibitorNet;
import org.processmining.models.graphbased.directed.petrinet.impl.StochasticNetImpl;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.bpmnminer.causalnet.CausalNet;
import org.processmining.plugins.bpmnminer.converter.CausalNetToPetrinet;
import org.processmining.plugins.bpmnminer.plugins.FodinaMinerPlugin;
import org.processmining.plugins.bpmnminer.types.MinerSettings;

import au.edu.qut.pm.stochastic.StochasticNetDescriptor;
import au.edu.qut.prom.helpers.StochasticPetriNetUtils;

public class ClassicFodinaMiner implements StochasticNetLogMiner {

	private static class StripFodinaSuffixCloner extends StochasticNetImpl{

		public StripFodinaSuffixCloner(String label) {
			super(label);
		}

		public TimedTransition addImmediateTransition(String label, double weight, int priority, String trainingData) {
			return super.addImmediateTransition(stripSuffix(label), weight, priority, trainingData);
		}
		
	    public TimedTransition addTimedTransition(String label, double weight, DistributionType type,
                String trainingData, double... distributionParameters) {
	    	return super.addTimedTransition(stripSuffix(label), weight,type,trainingData, distributionParameters);
	    }

		public static StochasticNet cloneFromPetriNet(Petrinet other) {
			StripFodinaSuffixCloner net = new StripFodinaSuffixCloner(other.getLabel());
			net.cloneFrom((AbstractResetInhibitorNet)other, true, true, true, true, true);
			for (Transition tran: net.getTransitions()) {
				if (tran instanceof TimedTransition) {
					((TimedTransition) tran).setDistributionType(DistributionType.IMMEDIATE);
				}
			}
			return net;
		}
		
		private static String stripSuffix(String label) {
			return label.replaceFirst("\\+complete", "");
		}
		
	}
	
	private static final Logger LOGGER = LogManager.getLogger();
	
	private StochasticNetDescriptor result = null;
	
	@Override
	public String getShortID() {
		return "fodina";
	}
	
	@Override
	public String getReadableID() {
		return "Fodina Miner";
	}

	@Override
	public void run(PluginContext uipc, XLog log, File outputModelFile) throws Exception {
		 XEventClassifier targetClassifier = new XEventNameClassifier();
		LOGGER.debug("Using classifier {}", targetClassifier);
		MinerSettings settings = new MinerSettings();
		settings.classifier = targetClassifier;
		Object[] fResult = FodinaMinerPlugin.runMiner(uipc, log, settings);
		CausalNet cnet = (CausalNet)fResult[0];		
		Object[] cResult = CausalNetToPetrinet.convert(uipc, cnet);
		Petrinet net = (Petrinet)cResult[0];
		
		Marking initialMarking = (Marking)cResult[1];		
		StochasticNet snet = StripFodinaSuffixCloner.cloneFromPetriNet(net);
		Marking sInitMarking = 
				StochasticPetriNetUtils.findEquivalentInitialMarking(initialMarking, snet);
		
		result = new StochasticNetDescriptor(net.getLabel(), snet, sInitMarking );
	}



	@Override
	public StochasticNetDescriptor getStochasticNetDescriptor() {
		return result;
	}
	
	@Override
	public boolean isStochasticNetProducer() {
		return false;
	}

		
}
