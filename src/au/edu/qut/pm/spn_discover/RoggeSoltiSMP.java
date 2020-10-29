package au.edu.qut.pm.spn_discover;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.petrinet.StochasticNet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.stochasticpetrinet.miner.StochasticMinerPlugin;

import au.edu.qut.pm.stochastic.StochasticNetDescriptor;

public class RoggeSoltiSMP implements StochasticNetLogMiner {
	
	private static Logger LOGGER = LogManager.getLogger();
	
	private StochasticNetDescriptor snDescriptor = null;

	@Override
	public String getShortID() {
		return "rssm";
	}
	
	public String getReadableID() {
		return "Rogge-Solti StochasticMinerPlugin";
	}
	
	public void run(PluginContext uipc, XLog log, File outputModelFile) throws Exception{
		Object[] objects = StochasticMinerPlugin.discoverStochNetModel((UIPluginContext)uipc, log);
		LOGGER.debug("Discovery complete");
		StochasticNet net = (StochasticNet) objects[0];
		Marking marking = (Marking) objects[1];
		snDescriptor = new StochasticNetDescriptor(outputModelFile.getName(),net, marking);
	}

	@Override
	public StochasticNetDescriptor getStochasticNetDescriptor() {
		return snDescriptor;
	}

	
}
