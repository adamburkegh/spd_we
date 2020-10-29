package au.edu.qut.pm.spn_estimator;

import org.processmining.models.graphbased.directed.petrinet.StochasticNet;

public class NoopEstimator extends AbstractFrequencyEstimator{
	
	@Override
	public void estimateWeights(StochasticNet net) {
	}

	@Override
	public String getShortID() {
		return "noop";
	}

	@Override
	public String getReadableID() {
		return "No Operation";
	}

}
