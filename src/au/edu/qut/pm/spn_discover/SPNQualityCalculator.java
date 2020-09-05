package au.edu.qut.pm.spn_discover;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.petrinet.StochasticNet;

public interface SPNQualityCalculator {

	public String getReadableId();
	public void calculate(PluginContext context, StochasticNet net, XLog log, 
			XEventClassifier classifier, TaskStats stats) throws Exception;

}