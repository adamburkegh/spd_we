package au.edu.qut.pm.alpha;

import org.processmining.alphaminer.abstractions.AlphaRobustAbstraction;
import org.processmining.framework.plugin.PluginContext;

import au.edu.qut.pm.spn_estimator.MeanScaledActivityPairRHWeightEstimator;
import au.edu.qut.pm.spn_estimator.WeightEstimator;

public class StochasticAlphaMinerMeanScaledActivityPairImpl<E> extends StochasticAlphaMinerImpl<E> {

	public StochasticAlphaMinerMeanScaledActivityPairImpl(AlphaRobustAbstraction<E> abstraction, PluginContext context) {
		super(abstraction, context);
	}
	
	@Override
	protected WeightEstimator createEstimator() {
		AlphaRobustAbstraction<E> abstraction = (AlphaRobustAbstraction<E>) getAbstraction();
		WeightEstimator estimator = 
				new MeanScaledActivityPairRHWeightEstimator<E>(abstraction.getDirectlyFollowsAbstraction(),
													   abstraction.getRobustActivityCount(),
													   abstraction.getStartActivityAbstraction(),
													   abstraction.getEndActivityAbstraction(),
													   transition2class);
		return estimator;
	}

}
