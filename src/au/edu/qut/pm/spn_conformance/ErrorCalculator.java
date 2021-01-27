package au.edu.qut.pm.spn_conformance;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;

import au.edu.qut.pm.spn_discover.SPNQualityCalculator;
import au.edu.qut.pm.spn_discover.TaskStats;
import au.edu.qut.pm.stochastic.StochasticNetDescriptor;

/**
 * Does nothing, unsuccessfully.
 * 
 * For testing.
 * 
 * @author burkeat
 *
 */
public class ErrorCalculator implements SPNQualityCalculator{

	@Override
	public String getReadableId() {
		return "Calculator Which Errors";
	}

	@Override
	public void calculate(PluginContext context, StochasticNetDescriptor net, XLog log, 
			XEventClassifier classifier, TaskStats stats) throws Exception 
	{
		throw new Exception("ErrorCalculator always errors");
	}

}
