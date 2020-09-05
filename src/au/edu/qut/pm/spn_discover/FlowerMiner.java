package au.edu.qut.pm.spn_discover;

import java.io.File;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.petrinet.StochasticNet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.TimedTransition;
import org.processmining.models.graphbased.directed.petrinet.impl.StochasticNetImpl;
import org.processmining.models.semantics.petrinet.Marking;

import au.edu.qut.xes.helpers.XESLogUtils;

public class FlowerMiner implements StochasticNetLogMiner {

	private StochasticNetDescriptor result = null;
	
	@Override 
	public String getShortID() {
		return "flower";
	}
	
	@Override
	public String getReadableID() {
		return "Flower Model With Unity Weights";
	}

	@Override
	public void run(PluginContext uipc, XLog log, File outputModelFile) throws Exception {
		XEventClassifier targetClassifier = XESLogUtils.detectNameBasedClassifier(log);		
		result = runMiner(uipc, log,targetClassifier);
	}

	public StochasticNetDescriptor runMiner(PluginContext uipc, XLog log, 
			XEventClassifier classifier) {
		StochasticNet net = minePetrinet(log.getInfo(classifier),classifier);
		return new StochasticNetDescriptor(net.getLabel(), net, new Marking());
	}

	@Override
	public StochasticNetDescriptor getStochasticNetDescriptor() {
		return result;
	}

	// Adapted from org.processmining.plugins.flowerMiner.FlowerMiner by Sander Leemans in 
	// InductiveMinerDeprecated package
	public static StochasticNet minePetrinet(XLogInfo logInfo, XEventClassifier classifier) {
		StochasticNet net = new StochasticNetImpl("flower");
		Place source = net.addPlace("source");
		Place sink = net.addPlace("sink");
		Place stigma = net.addPlace("stigma");

		// Transition start = net.addTimedTransition("start");
		TimedTransition start = net.addImmediateTransition("start");
		start.setInvisible(true);
		start.setWeight(1.0);
		net.addArc(source, start);
		net.addArc(start, stigma);

		TimedTransition end = net.addImmediateTransition("end");
		end.setInvisible(true);
		end.setWeight(1.0);
		net.addArc(stigma, end);
		net.addArc(end, sink);

		for (XEventClass activity : logInfo.getEventClasses().getClasses()) {
			TimedTransition t = net.addImmediateTransition(activity.toString());
			t.setWeight(1.0);
			net.addArc(stigma, t);
			net.addArc(t, stigma);
		}

		return net;
	}

	@Override
	public boolean isStochasticNetProducer() {
		// Arguable, as it does set weights. But this mainly indicates whether it's worth 
		// running estimators
		return false;
	}


}
