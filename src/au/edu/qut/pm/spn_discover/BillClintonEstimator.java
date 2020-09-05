package au.edu.qut.pm.spn_discover;

import java.util.HashMap;
import java.util.Map;

import org.processmining.models.graphbased.directed.petrinet.StochasticNet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.TimedTransition;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

import au.edu.qut.prom.helpers.StochasticPetriNetUtils;

/**
 * "When he comes to a fork in the road, he takes the fork" -- Jesse Jackson on Bill Clinton
 * 
 * Referred to as ForkDistributionEstimator in the accompanying paper.
 * 
 * @author burkeat
 *
 * @param <E>
 */
public class BillClintonEstimator extends AbstractFrequencyEstimator{

	@Override
	public String getShortID() {
		return "bce";
	}

	@Override
	public String getReadableID() {
		return "Fork Distributed (Bill Clinton) Estimator";
	}
	
	@Override
	public void estimateWeights(StochasticNet net) {
		projectedFrequencyWeights(net);
	}

	private void projectedFrequencyWeights(StochasticNet net ) {
		Map<Place,Double> placeWeights = new HashMap<>();
		for (Transition tran: net.getTransitions()) {
			for (Place succPlace: StochasticPetriNetUtils.successors(tran)) {
				double totalPairWeight = 0;
				for (Transition succTran: StochasticPetriNetUtils.successors(succPlace)) {
					totalPairWeight += loadFollowFrequency(tran, succTran);
				}
				if (placeWeights.containsKey(succPlace)){
					totalPairWeight += placeWeights.get(succPlace);
				}
				placeWeights.put(succPlace, totalPairWeight);
			}
			// Reset transition weights
			TimedTransition transition = (TimedTransition)tran;
			transition.setWeight(0);
		}
		for (Place place: net.getPlaces()) {
			if (net.getGraph().getInEdges(place).isEmpty()) {
				// Initialize start place
				placeWeights.put(place, (double)traceCount);
			}
			if (placeWeights.get(place) == 0) {
				placeWeights.put(place, 1.0);
			}
			double tranTotal = 0;
			for (Transition tran: StochasticPetriNetUtils.successors(place)) {
				tranTotal += loadActivityFrequency(tran);
			}
			for (Transition tran: StochasticPetriNetUtils.successors(place)) {
				double freq = loadActivityFrequency(tran);
				double placeBudget = placeWeights.get(place);
				double weight = placeBudget * freq / tranTotal;
				TimedTransition transition = (TimedTransition)tran;
				transition.setWeight( transition.getWeight() + weight);
			}
		}
	}

	
}
