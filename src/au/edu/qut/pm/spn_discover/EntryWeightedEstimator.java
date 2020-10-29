package au.edu.qut.pm.spn_discover;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.processmining.models.graphbased.directed.petrinet.StochasticNet;
import org.processmining.models.graphbased.directed.petrinet.StochasticNet.DistributionType;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.TimedTransition;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

import au.edu.qut.pm.spn_estimator.WeightEstimator;
import au.edu.qut.prom.helpers.StochasticPetriNetUtils;

public class EntryWeightedEstimator implements WeightEstimator {

	private static final Logger LOGGER = LogManager.getLogger();
	
	private Map<String,Long> activityFrequencies;
	
	public EntryWeightedEstimator(Map<String,Long> activityFrequencies) {
		this.activityFrequencies = activityFrequencies;
	}
	
	@Override
	public void estimateWeights(StochasticNet net) {
		Map<Place,Double> placeEntryRatios = new HashMap<>();
		for (Place place: net.getPlaces()) {
			double totalPred = 0;
			for (Transition predTran: StochasticPetriNetUtils.predecessors(place)) {
				totalPred += activityFrequencies.get(predTran.getLabel());
			}
			double totalSucc = 0;
			for (Transition succTran: StochasticPetriNetUtils.successors(place)) {
				totalSucc += activityFrequencies.get(succTran.getLabel());
			}
			if (totalPred > 0 && totalSucc > 0) {
				placeEntryRatios.put(place, totalPred/totalSucc);
				if (totalPred != totalSucc)
					LOGGER.debug("non-unity ratio");
			}else {
				placeEntryRatios.put(place, 1.0d);
			}
		}
		LOGGER.debug("Place entry ratios: {}",placeEntryRatios);
		for (Transition tran: net.getTransitions()) {
			TimedTransition transition = (TimedTransition)tran;
			double sumEntryRatio = 0d;
			Collection<Place> predecessors = StochasticPetriNetUtils.predecessors(transition);
			for (Place place: predecessors) {
				sumEntryRatio += placeEntryRatios.get(place);
			}
			double weight = predecessors.size() * activityFrequencies.get(tran.getLabel()) * sumEntryRatio ; 
			transition.setWeight( weight );
			transition.setDistributionType(DistributionType.IMMEDIATE);
		}
	}

	
}
