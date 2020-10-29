package au.edu.qut.pm.spn_discover;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.petrinet.StochasticNet;
import org.processmining.models.semantics.petrinet.Marking;

import au.edu.qut.pm.ars.spn.StochasticMinerPlugin;
import au.edu.qut.pm.stochastic.StochasticNetDescriptor;

/**
 * Very like <class>RoggeSoltiSMP</class>, but invoking a local copy which has additional tracing
 * and tweaks
 * 
 * @author burkeat
 *
 */
public class RoggeSoltiSMPTracing implements StochasticNetLogMiner {
	
	private static Logger LOGGER = LogManager.getLogger();
	
	private StochasticNetDescriptor snDescriptor = null;

	public String getShortID() {
		return "rssmt";
	}
	
	public String getReadableID() {
		return "Rogge-Solti StochasticMinerPlugin (Tracing)";
	}
	
	public void run(PluginContext uipc, XLog log, File outputModelFile) throws Exception{
		// This is the one Sander uses in his CAISE paper
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
