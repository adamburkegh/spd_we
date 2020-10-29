package au.edu.qut.pm.spn_estimator;

import static au.edu.qut.prom.helpers.StochasticPetriNetUtils.findAllSuccessors;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.processmining.logabstractions.models.ColumnAbstraction;
import org.processmining.logabstractions.models.MatrixAbstraction;
import org.processmining.models.graphbased.directed.petrinet.StochasticNet;
import org.processmining.models.graphbased.directed.petrinet.elements.TimedTransition;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

public class MeanScaledActivityPairRHWeightEstimator<E> implements WeightEstimator {

	private MatrixAbstraction<E> followsFrequency;
	private ColumnAbstraction<E> activityFrequency;
	private ColumnAbstraction<E> startFrequency;
	private ColumnAbstraction<E> endFrequency;
	private Map<Transition, E> transition2class = new HashMap<Transition, E>();

	public MeanScaledActivityPairRHWeightEstimator(MatrixAbstraction<E> followsFrequency,
			ColumnAbstraction<E> activityFrequency,
			ColumnAbstraction<E> startFrequency, ColumnAbstraction<E> endFrequency, 
			Map<Transition, E> transition2class) {
		this.followsFrequency = followsFrequency;
		this.activityFrequency = activityFrequency;
		this.startFrequency = startFrequency;
		this.endFrequency = endFrequency;
		this.transition2class = transition2class;
	}

	@Override
	public void estimateWeights(StochasticNet net) {
		edgePairWeights(net);
	}

	private void edgePairWeights(StochasticNet net) {
		double frequencyTotal = 0;
		for (Transition tran : net.getTransitions()) {
			frequencyTotal += activityFrequency.getValue(transition2class.get(tran));
		}
		double mean = frequencyTotal / net.getTransitions().size();
		for (Transition tran : net.getTransitions()) {
			E tranEC = transition2class.get(tran);
			TimedTransition transition = (TimedTransition) tran;
			Collection<Transition> successors = findAllSuccessors(transition);
			double successorWeight = 0;
			for (Transition succ : successors) {
				successorWeight += followsFrequency.getValue(tranEC, transition2class.get(succ));
			}
			double weight = (successorWeight
					+ startFrequency.getValue(tranEC) + endFrequency.getValue(tranEC) ) 
					/ mean;
			transition.setWeight(weight);
		}
	}

}
