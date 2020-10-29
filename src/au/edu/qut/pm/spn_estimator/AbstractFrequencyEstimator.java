package au.edu.qut.pm.spn_estimator;

import java.util.HashMap;
import java.util.Map;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.framework.util.Pair;
import org.processmining.models.graphbased.directed.petrinet.StochasticNet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

import au.edu.qut.pm.stochastic.StochasticNetCloner;

public abstract class AbstractFrequencyEstimator implements LogSourcedWeightEstimator {

	protected Map<Pair<String,String>,Double> followsFrequency = new HashMap<>();
	protected Map<String,Double> activityFrequency = new HashMap<>();
	protected Map<String,Double> startFrequency = new HashMap<>();
	protected Map<String,Double> endFrequency = new HashMap<>();
	protected Map<String, XEventClass> activity2class = new HashMap<String, XEventClass>();
	protected int traceCount = 0;


	public void scanLog(XLog log, XEventClassifier classifier) {
		XEventClasses classes = XEventClasses.deriveEventClasses(classifier, log);
		for (int i=0; i<classes.size(); i++) {
			XEventClass eventClass = classes.getByIndex(i);
			activity2class.put(eventClass.getId(),eventClass);
		}
		traceCount = log.size();
		double[][] dfa = new double[classes.size()][classes.size()]; // directly follows (count)
		double[] starts = new double[classes.size()]; // start activity
		double[] ends = new double[classes.size()]; // end activity
		double[] ac = new double[classes.size()]; // activity count
		for (XTrace trace : log) {
			if (!trace.isEmpty()) {
				starts[classes.getClassOf(trace.get(0))
						.getIndex()] += 1.0;
				ends[classes.getClassOf(trace.get(trace.size() - 1))
						.getIndex()] += 1.0;
				if (trace.size() == 1) {
					ac[classes.getClassOf(trace.get(0)).getIndex()]  += 1.0;
				}
				for (int i = 0; i < trace.size() - 1; i++) {
					XEventClass from = classes.getClassOf(trace.get(i));
					XEventClass to = classes.getClassOf(trace.get(i + 1));
					dfa[from.getIndex()][to.getIndex()] += 1.0;
					ac[from.getIndex()] += 1.0;
					if (i == trace.size() - 2) { // count final activity as well
						ac[to.getIndex()] += 1.0;
					}
				}
			}
		}
		for (int i=0; i<classes.size(); i++) {
			String label = classes.getByIndex(i).getId();
			startFrequency.put(label, starts[i]);
			endFrequency.put(label, ends[i]);
			activityFrequency.put(label, ac[i]);
			for (int j=0;j<classes.size(); j++) {
				followsFrequency.put(
						new Pair<String,String>(label,classes.getByIndex(j).getId()), 
						dfa[i][j]);
			}
		}
	}

	@Override
	public StochasticNet estimateWeights(AcceptingPetriNet pnet, XLog log, XEventClassifier classifier) {
		scanLog(log,classifier);
		StochasticNet snet = StochasticNetCloner.cloneFromPetriNet(pnet.getNet());
		estimateWeights(snet); 
		return snet;
	}

	protected double loadFollowFrequency(Transition tran, Transition succTran) {
		Pair<String,String> key = new Pair<>(tran.getLabel(),succTran.getLabel());
		Double value = followsFrequency.get(key);
		if (value == null) {
			followsFrequency.put(key, 0.0d);
			return 0.0;
		}
		return value;
	}

	protected double loadActivityFrequency(Transition tran) {
		Double value = activityFrequency.get(tran.getLabel());
		if (value == null) {
			activityFrequency.put(tran.getLabel(), 1.0d);
			return 1.0;
		}
		return value;
	}

	protected double loadZeroableFrequency(Transition tran, Map<String,Double> freq) {
		Double value = freq.get(tran.getLabel());
		if (value == null) {
			freq.put(tran.getLabel(), 0.0d);
			return 0.0;
		}
		return value;
	}

	
}
