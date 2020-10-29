package au.edu.qut.pm.alpha;

import org.processmining.alphaminer.abstractions.AlphaClassicAbstraction;
import org.processmining.framework.plugin.PluginContext;

import au.edu.qut.pm.spn_estimator.ActivityPairRHWeightEstimator;
import au.edu.qut.pm.spn_estimator.WeightEstimator;

public class StochasticAlphaMinerActivityPairRHImpl<E> extends StochasticAlphaMinerImpl<E> {

	public StochasticAlphaMinerActivityPairRHImpl(AlphaClassicAbstraction<E> abstraction, PluginContext context) {
		super(abstraction, context);
	}
	
	@Override
	protected WeightEstimator createEstimator() {
		WeightEstimator estimator = 
				new ActivityPairRHWeightEstimator<E>(getAbstraction().getDirectlyFollowsAbstraction(),
											 getAbstraction().getStartActivityAbstraction(),
											 getAbstraction().getEndActivityAbstraction(),
											 transition2class);
		return estimator;
	}

}
