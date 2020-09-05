package au.edu.qut.pm.spn_discover;

import static au.edu.qut.prom.helpers.StochasticPetriNetUtils.findAllPredecessors;

import java.util.Collection;

import org.processmining.models.graphbased.directed.petrinet.StochasticNet;
import org.processmining.models.graphbased.directed.petrinet.elements.TimedTransition;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

public class ActivityPairLHEstimator extends AbstractFrequencyEstimator {
	
	@Override
	public String getShortID() {
		return "aplh";
	}

	@Override
	public String getReadableID() {
		return "Activity Pair Left-Handed Estimator";
	}
	
	@Override
	public void estimateWeights(StochasticNet net) {
		edgePairWeights(net);
	}

	private void edgePairWeights(StochasticNet net ) {
		for (Transition tran: net.getTransitions()) {
			TimedTransition transition = (TimedTransition)tran;
			Collection<Transition> predecessors = findAllPredecessors(transition);
			double predecessorWeight = 0;
			for (Transition pred: predecessors) {
				predecessorWeight += loadFollowFrequency(pred, tran);
			}
			double weight = predecessorWeight
					+ loadZeroableFrequency(tran, startFrequency)
					+ loadZeroableFrequency(tran, endFrequency);
			transition.setWeight(weight > 0.0 ? weight: 1.0);
		}
	}

}
