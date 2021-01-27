package au.edu.qut.pm.spn_discover;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;

import au.edu.qut.pm.stochastic.StochasticNetDescriptor;

public interface SPNQualityCalculator {

	public String getReadableId();
	public void calculate(PluginContext context, StochasticNetDescriptor net, XLog log, 
			XEventClassifier classifier, TaskStats stats) throws Exception;

}