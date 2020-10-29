package au.edu.qut.pm.stochastic;

import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.StochasticNet;
import org.processmining.models.graphbased.directed.petrinet.elements.TimedTransition;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.AbstractResetInhibitorNet;
import org.processmining.models.graphbased.directed.petrinet.impl.StochasticNetImpl;

public class StochasticNetCloner extends StochasticNetImpl{

	public StochasticNetCloner(String label) {
		super(label);
	}

	public static StochasticNet cloneFromPetriNet(Petrinet other) {
		StochasticNetCloner net = new StochasticNetCloner(other.getLabel());
		net.cloneFrom((AbstractResetInhibitorNet)other, true, true, true, true, true);
		for (Transition tran: net.getTransitions()) {
			if (tran instanceof TimedTransition) {
				((TimedTransition) tran).setDistributionType(DistributionType.IMMEDIATE);
			}
		}
		return net;
	}
	
}