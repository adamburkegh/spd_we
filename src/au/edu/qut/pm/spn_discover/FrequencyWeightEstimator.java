package au.edu.qut.pm.spn_discover;

import java.util.Map;

import org.processmining.models.graphbased.directed.petrinet.StochasticNet;
import org.processmining.models.graphbased.directed.petrinet.StochasticNet.DistributionType;
import org.processmining.models.graphbased.directed.petrinet.elements.TimedTransition;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

public class FrequencyWeightEstimator implements WeightEstimator {

	private Map<String,Long> activityFrequencies;
	
	public FrequencyWeightEstimator(Map<String,Long> activityFrequencies) {
		this.activityFrequencies = activityFrequencies;
	}
	
	@Override
	public void estimateWeights(StochasticNet net) {
		for (Transition tran: net.getTransitions()) {
			TimedTransition transition = (TimedTransition)tran; 
			transition.setWeight( activityFrequencies.get(tran.getLabel()) );
			transition.setDistributionType(DistributionType.IMMEDIATE);
		}
	}

}
