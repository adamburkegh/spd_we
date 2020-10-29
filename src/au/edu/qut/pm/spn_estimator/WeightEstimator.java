package au.edu.qut.pm.spn_estimator;

import org.processmining.models.graphbased.directed.petrinet.StochasticNet;

public interface WeightEstimator {

	public void estimateWeights(StochasticNet net);
	
}
