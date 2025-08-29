package au.edu.qut.pm.spn_discover;

import java.io.File;

import au.edu.qut.pm.util.LogManager;
import au.edu.qut.pm.util.Logger;
import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.alphaminer.abstractions.AlphaAbstractionFactory;
import org.processmining.alphaminer.abstractions.AlphaClassicAbstraction;
import org.processmining.alphaminer.algorithms.AlphaMiner;
import org.processmining.alphaminer.algorithms.AlphaMinerFactory;
import org.processmining.alphaminer.parameters.AlphaMinerParameters;
import org.processmining.alphaminer.parameters.AlphaVersion;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.util.Pair;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.StochasticNet;
import org.processmining.models.semantics.petrinet.Marking;

import au.edu.qut.pm.stochastic.StochasticNetCloner;
import au.edu.qut.pm.stochastic.StochasticNetDescriptor;
import au.edu.qut.xes.helpers.XESLogUtils;

public class ClassicAlphaMiner implements StochasticNetLogMiner {

	private static final Logger LOGGER = LogManager.getLogger();
	
	private StochasticNetDescriptor result = null;
	
	@Override
	public String getShortID() {
		return "alphacl";
	}
	
	@Override
	public String getReadableID() {
		return "Alpha Classic";
	}

	@Override
	public void run(PluginContext uipc, XLog log, File outputModelFile) throws Exception {
		XEventClassifier targetClassifier = XESLogUtils.detectNameBasedClassifier(log);
		LOGGER.debug("Using classifier {}", targetClassifier);
		AlphaClassicAbstraction<XEventClass> abstraction = AlphaAbstractionFactory.createAlphaClassicAbstraction(log, targetClassifier);
		AlphaMinerParameters parameters = new AlphaMinerParameters(AlphaVersion.CLASSIC);
		AlphaMiner<XEventClass, AlphaClassicAbstraction<XEventClass>, AlphaMinerParameters> miner = 
				AlphaMinerFactory.createAlphaClassicMiner(uipc, parameters, abstraction);
		Pair<Petrinet, Marking> alphaOutput = miner.run();
		Petrinet pnet = alphaOutput.getFirst();
		StochasticNet net = StochasticNetCloner.cloneFromPetriNet(pnet);
		result = new StochasticNetDescriptor(net.getLabel(), net, alphaOutput.getSecond());
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
