package au.edu.qut.pm.alpha;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.processmining.alphaminer.abstractions.AlphaClassicAbstraction;
import org.processmining.alphaminer.algorithms.AlphaClassicMinerImpl;
import org.processmining.alphaminer.models.AlphaPairImpl;
import org.processmining.alphaminer.parameters.AlphaMinerParameters;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.util.Pair;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.StochasticNet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.StochasticNetImpl;
import org.processmining.models.semantics.petrinet.Marking;

import au.edu.qut.pm.spn_estimator.WeightEstimator;

/**
 * Extension of AlphaMiner to natively calculate transition weights. Markings are not
 * populated.
 * 
 * @author burkeat
 *
 * @param <E>
 */
public abstract class StochasticAlphaMinerImpl<E> 
	extends AlphaClassicMinerImpl<E,AlphaClassicAbstraction<E>,AlphaMinerParameters> implements StochasticAlphaMiner<E>
{

	protected final Map<Transition, E> transition2class = new HashMap<Transition, E>();
		
	public StochasticAlphaMinerImpl(AlphaClassicAbstraction<E> abstraction, PluginContext context) {
		super(new AlphaMinerParameters(), abstraction, context);
	}

	/**
	 * Similar to the Alpha Classic miner in the parent method, but with extra steps to calculate
	 * weightings.
	 */
	@Override
	public Pair<Petrinet, Marking> run() {
		Pair<StochasticNet, Marking> spnPair = runStochasticMiner();
		Pair<Petrinet,Marking> result = new Pair<>(spnPair.getFirst(),spnPair.getSecond());
		return result;
	}

	protected void addTransitions(StochasticNet net) {
		// Add transitions, but don't set weights
		AlphaClassicAbstraction<E> abstraction = getAbstraction();
		Map<E, Transition> class2transition = getEventClassToTransitionMapping();
		for (int i = 0; i < abstraction.getEventClasses().length; i++) {
			E eventClass = abstraction.getEventClass(i);
			Transition transition = net.addImmediateTransition(eventClass.toString());
			class2transition.put(eventClass, transition);
			transition2class.put(transition, eventClass);
		}
	}

	protected void assignWeights(StochasticNet net) {
		WeightEstimator estimator = createEstimator();
		estimator.estimateWeights(net);
	}
	
	protected abstract WeightEstimator createEstimator();

	@Override
	public Pair<StochasticNet, Marking> runStochasticMiner() {
		getProgress().setMinimum(0);
		getProgress().setMaximum(5);
		getProgress().setIndeterminate(false);
		getProgress().inc();
		final List<AlphaPairImpl<Collection<E>, Collection<E>>> result = alphaExpansion(
				getAbstraction().getCausalAbstraction(), getAbstraction().getUnrelatedAbstraction(),
				getAbstraction().getLengthOneLoopAbstraction());
		StochasticNet net = new StochasticNetImpl("SPN (Stochalpha)");
		Marking iMarking = new Marking();
		Marking fMarking = new Marking();
		addTransitions(net);
		getProgress().inc();
		addPlaces(net, result);
		getProgress().inc();
		addInitialPlace(net, getAbstraction().getStartActivityAbstraction(), iMarking);
		addFinalPlace(net, getAbstraction().getEndActivityAbstraction(), fMarking);
		getProgress().inc();
		assignWeights(net);
		getProgress().inc();
		Pair<StochasticNet,Marking> resultPair = new Pair<StochasticNet,Marking>(net, new Marking());
		return resultPair;
	}

	

	
}
