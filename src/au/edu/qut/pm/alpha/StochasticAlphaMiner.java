package au.edu.qut.pm.alpha;

import org.processmining.framework.util.Pair;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.StochasticNet;
import org.processmining.models.semantics.petrinet.Marking;

public interface StochasticAlphaMiner<E> {

	/**
	 * Similar to the Alpha Classic miner in the parent method, but with extra steps to calculate
	 * weightings.
	 */
	Pair<Petrinet, Marking> run();

	Pair<StochasticNet, Marking> runStochasticMiner();

}