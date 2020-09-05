package au.edu.qut.pm.spn_discover;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.alphaminer.abstractions.AlphaClassicAbstraction;
import org.processmining.alphaminer.abstractions.AlphaRobustAbstraction;
import org.processmining.alphaminer.algorithms.AlphaMiner;
import org.processmining.alphaminer.algorithms.AlphaMinerFactory;
import org.processmining.alphaminer.parameters.AlphaMinerParameters;
import org.processmining.alphaminer.parameters.AlphaRobustMinerParameters;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.util.Pair;
import org.processmining.logabstractions.models.ColumnAbstraction;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.StochasticNet;
import org.processmining.models.semantics.petrinet.Marking;

import au.edu.qut.pm.alpha.AlphaAbstractionFactoryWithFixes;
import au.edu.qut.xes.helpers.XESLogUtils;

public class StochalphaFreqMiner implements StochasticNetLogMiner {

	private Logger LOGGER = LogManager.getLogger();
	
	private StochasticNetDescriptor result = null;
	
	@Override 
	public String getShortID() {
		return "sfm";
	}
	
	@Override
	public String getReadableID() {
		return "Alpha Miner Classic + Post Frequency";
	}

	@Override
	public void run(PluginContext uipc, XLog log, File outputModelFile) throws Exception {
		XEventClassifier targetClassifier = XESLogUtils.detectNameBasedClassifier(log);		
		result = runMiner(uipc, log,targetClassifier);
	}

	public StochasticNetDescriptor runMiner(PluginContext uipc, XLog log, 
			XEventClassifier classifier) {
		AlphaRobustMinerParameters parameters = new AlphaRobustMinerParameters(0.0,0.0,0.0);
		AlphaRobustAbstraction<XEventClass> abstraction = 
				AlphaAbstractionFactoryWithFixes.createAlphaRobustAbstraction(log,classifier, 
						parameters);
		AlphaMiner<XEventClass,AlphaClassicAbstraction<XEventClass>,AlphaMinerParameters> miner = 
				AlphaMinerFactory.createAlphaClassicMiner(uipc, parameters, abstraction);
		Pair<Petrinet, Marking> alphaOutput = miner.run();
		Petrinet pnet = alphaOutput.getFirst();
		LOGGER.debug("Discovered net {} with {} edges", pnet, pnet.getEdges().size());
		StochasticNet net = StochasticNetCloner.cloneFromPetriNet(pnet);		
		WeightEstimator estimator = new FrequencyWeightEstimator(
				toActivityFrequency( abstraction.getRobustActivityCount() ) );
		estimator.estimateWeights(net);
		return new StochasticNetDescriptor(net.getLabel(), net, alphaOutput.getSecond());
	}

	@Override
	public StochasticNetDescriptor getStochasticNetDescriptor() {
		return result;
	}
	
	private Map<String,Long> toActivityFrequency(ColumnAbstraction<XEventClass> colFreq){
		Map<String,Long> freqMap = new HashMap<>();
		for (int i = 0; i < colFreq.getEventClasses().length; i++) {
			 XEventClass eventClass = colFreq.getEventClass(i);
			 freqMap.put(eventClass.getId(), Math.round( colFreq.getValue(i) ) );
		}
		return freqMap;
	}



}
