package au.edu.qut.pm.spn_discover;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.util.Pair;
import org.processmining.models.graphbased.directed.petrinet.StochasticNet;
import org.processmining.models.semantics.petrinet.Marking;

import au.edu.qut.pm.alpha.StochasticAlphaMiner;
import au.edu.qut.pm.alpha.StochasticAlphaMinerFactory;
import au.edu.qut.pm.stochastic.StochasticNetDescriptor;
import au.edu.qut.xes.helpers.XESLogUtils;

public class StochalphaBillClintonMiner implements StochasticNetLogMiner {

	private Logger LOGGER = LogManager.getLogger();
	
	private StochasticNetDescriptor result = null;
	
	@Override 
	public String getShortID() {
		return "sambc";
	}
	
	@Override
	public String getReadableID() {
		return "Stochastic Alpha Miner (Bill Clinton)";
	}

	@Override
	public void run(PluginContext uipc, XLog log, File outputModelFile) throws Exception {
		XEventClassifier targetClassifier = XESLogUtils.detectNameBasedClassifier(log);
		LOGGER.debug("Using classifier {}", targetClassifier);
		StochasticAlphaMiner<XEventClass> miner =
				StochasticAlphaMinerFactory.createBillClintonSAM(targetClassifier, log, uipc);
		Pair<StochasticNet, Marking> alphaOutput = miner.runStochasticMiner();
		StochasticNet pnet = alphaOutput.getFirst();
		LOGGER.debug("Discovered net {} with {} edges", pnet, pnet.getEdges().size());
		result = new StochasticNetDescriptor(pnet.getLabel(), pnet, alphaOutput.getSecond());
	}


	@Override
	public StochasticNetDescriptor getStochasticNetDescriptor() {
		return result;
	}

}
