package au.edu.qut.pm.spn_estimator;

import static au.edu.qut.prom.helpers.StochasticPetriNetUtils.findAllSuccessors;

import java.util.Collection;

import org.processmining.models.graphbased.directed.petrinet.StochasticNet;
import org.processmining.models.graphbased.directed.petrinet.elements.TimedTransition;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

public class MeanScaledActivityPairRHEstimator extends AbstractFrequencyEstimator {

	@Override
	public String getShortID() {
		return "msaprh";
	}

	@Override
	public String getReadableID() {
		return "Mean Scaled Activity Pair Estimator";
	}
	
	@Override
	public void estimateWeights(StochasticNet net) {
		edgePairWeights(net);
	}

	private void edgePairWeights(StochasticNet net) {
		double frequencyTotal = 0;
		for (Transition tran : net.getTransitions()) {
			frequencyTotal += loadZeroableFrequency(tran,activityFrequency);
		}
		if (frequencyTotal == 0) {
			frequencyTotal = 1.0;
		}
		double mean = frequencyTotal / net.getTransitions().size();
		for (Transition tran : net.getTransitions()) {
			TimedTransition transition = (TimedTransition) tran;
			Collection<Transition> successors = findAllSuccessors(transition);
			double successorWeight = 0;
			for (Transition succ : successors) {
				successorWeight += loadFollowFrequency(tran,succ);
			}
			double weight = (successorWeight
					+ loadZeroableFrequency(tran, startFrequency)
					+ loadZeroableFrequency(tran, endFrequency)) 
					/ mean;
			if (weight == 0)
				weight = 1.0;
			transition.setWeight(weight);
		}
	}

}
