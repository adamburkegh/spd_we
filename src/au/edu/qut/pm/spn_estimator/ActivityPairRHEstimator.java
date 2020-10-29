package au.edu.qut.pm.spn_estimator;

import static au.edu.qut.prom.helpers.StochasticPetriNetUtils.findAllSuccessors;

import java.util.Collection;

import org.processmining.models.graphbased.directed.petrinet.StochasticNet;
import org.processmining.models.graphbased.directed.petrinet.elements.TimedTransition;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

public class ActivityPairRHEstimator extends AbstractFrequencyEstimator {
	
	@Override
	public String getShortID() {
		return "aprh";
	}

	@Override
	public String getReadableID() {
		return "Activity Pair Right-Handed Estimator";
	}
	
	@Override
	public void estimateWeights(StochasticNet net) {
		edgePairWeights(net);
	}

	private void edgePairWeights(StochasticNet net ) {
		for (Transition tran: net.getTransitions()) {
			TimedTransition transition = (TimedTransition)tran;
			Collection<Transition> successors = findAllSuccessors(transition);
			double successorWeight  = 0;
			for (Transition succ: successors) {
				successorWeight += loadFollowFrequency(tran,succ);
			}
			double weight = successorWeight 
					+ loadZeroableFrequency(tran, startFrequency)
					+ loadZeroableFrequency(tran, endFrequency);
			transition.setWeight(weight > 0.0 ? weight: 1.0);
		}
	}

}
