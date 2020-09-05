package au.edu.qut.pm.spn_discover;

import org.processmining.models.graphbased.directed.petrinet.StochasticNet;

public interface WeightEstimator {

	public void estimateWeights(StochasticNet net);
	
}
