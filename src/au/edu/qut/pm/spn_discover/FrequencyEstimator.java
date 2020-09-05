package au.edu.qut.pm.spn_discover;

import org.processmining.models.graphbased.directed.petrinet.StochasticNet;
import org.processmining.models.graphbased.directed.petrinet.StochasticNet.DistributionType;
import org.processmining.models.graphbased.directed.petrinet.elements.TimedTransition;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

public class FrequencyEstimator extends AbstractFrequencyEstimator{
	
	@Override
	public void estimateWeights(StochasticNet net) {
		for (Transition tran: net.getTransitions()) {
			TimedTransition transition = (TimedTransition)tran;
			Double freq = activityFrequency.get(tran.getLabel());
			if (freq == null){
				freq = 1.0;
			}
			transition.setWeight( freq );
			transition.setDistributionType(DistributionType.IMMEDIATE);
		}
	}

	@Override
	public String getShortID() {
		return "fe";
	}

	@Override
	public String getReadableID() {
		return "Frequency Estimator";
	}

}
