package au.edu.qut.pm.spn_discover;

import java.util.HashMap;
import java.util.Map;

import org.processmining.logabstractions.models.ColumnAbstraction;
import org.processmining.logabstractions.models.MatrixAbstraction;
import org.processmining.models.graphbased.directed.petrinet.StochasticNet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.TimedTransition;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

import au.edu.qut.prom.helpers.StochasticPetriNetUtils;

/**
 * "When he comes to a fork in the road, he takes the fork" -- Jesse Jackson on Bill Clinton
 * 
 * @author burkeat
 *
 * @param <E>
 */
public class BillClintonWeightEstimator<E> implements WeightEstimator{

	private MatrixAbstraction<E> followsFrequency;
	private ColumnAbstraction<E> activityFrequency;
	private ColumnAbstraction<E> startFrequency;
	private Map<Transition, E> transition2class = new HashMap<Transition, E>();
	
	public BillClintonWeightEstimator(MatrixAbstraction<E> followsFrequency,
			ColumnAbstraction<E> activityFrequency,
			ColumnAbstraction<E> startFrequency,
			Map<Transition, E> transition2class) {
		this.followsFrequency = followsFrequency;
		this.activityFrequency = activityFrequency;
		this.startFrequency = startFrequency;
		this.transition2class = transition2class;
	}
	
	@Override
	public void estimateWeights(StochasticNet net) {
		projectedFrequencyWeights(net);
	}

	private void projectedFrequencyWeights(StochasticNet net ) {
		Map<Place,Double> placeWeights = new HashMap<>();
		for (Transition tran: net.getTransitions()) {
			E tranEC = transition2class.get(tran);
			for (Place succPlace: StochasticPetriNetUtils.successors(tran)) {
				double totalPairWeight = 0;
				for (Transition succTran: StochasticPetriNetUtils.successors(succPlace)) {
					totalPairWeight += followsFrequency.getValue(tranEC, 
			  				transition2class.get(succTran));
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
		double traceCount = 0; // This would be slightly more efficient if passed in 
		for (int i=0; i<startFrequency.getColumn().length; i++) {
			traceCount += startFrequency.getColumn()[i];
		}
		for (Place place: net.getPlaces()) {
			if (net.getGraph().getInEdges(place).isEmpty()) {
				// Initialize start place
				placeWeights.put(place, traceCount);
			}
			double tranTotal = 0;
			for (Transition tran: StochasticPetriNetUtils.successors(place)) {
				E tranEC = transition2class.get(tran);
				tranTotal += activityFrequency.getValue(tranEC);
			}
			for (Transition tran: StochasticPetriNetUtils.successors(place)) {
				E tranEC = transition2class.get(tran);
				double freq = activityFrequency.getValue(tranEC);
				double placeBudget = placeWeights.get(place);
				double weight = placeBudget * freq / tranTotal;
				TimedTransition transition = (TimedTransition)tran;
				transition.setWeight( transition.getWeight() + weight);
			}
		}
	}
	
}
