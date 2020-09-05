package au.edu.qut.pm.spn_discover;

import static au.edu.qut.prom.helpers.StochasticPetriNetUtils.findAllSiblings;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.processmining.logabstractions.models.ColumnAbstraction;
import org.processmining.models.graphbased.directed.petrinet.StochasticNet;
import org.processmining.models.graphbased.directed.petrinet.elements.TimedTransition;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

/**
 * Only well-defined for free-choice nets.
 * 
 * @author burkeat
 *
 * @param <E>
 */
public class EdgeStructuredEstimator<E> implements WeightEstimator{

	private ColumnAbstraction<E> frequency; 
	private Map<Transition, E> transition2class;
	
	public EdgeStructuredEstimator(ColumnAbstraction<E> frequency,
			Map<Transition, E> transition2class) 
	{
		this.frequency = frequency;
		this.transition2class = transition2class;
	}
	
	@Override
	public void estimateWeights(StochasticNet net) {
		edgeStructuredWeights(net);
	}

	private void edgeStructuredWeights(StochasticNet net ) {
		Map<Transition,Integer> transitionSiblingFreq = new HashMap<>();
		for (Transition transition: net.getTransitions()) {
			Collection<Transition> siblings = findAllSiblings(transition);
			int totalSiblingFreq = 0;
			for (Transition sibling: siblings) {
				totalSiblingFreq += 
						frequency.getValue(transition2class.get(sibling));
			}
			transitionSiblingFreq.put(transition, totalSiblingFreq);
		}
		for (Transition tran: net.getTransitions()) {
			TimedTransition transition = (TimedTransition)tran; 
			transition.setWeight(  
						frequency.getValue(transition2class.get(transition))
						/ (double)transitionSiblingFreq.get(transition) );
		}
	}
	
}
