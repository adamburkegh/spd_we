package au.edu.qut.pm.alpha;

import org.processmining.alphaminer.abstractions.AlphaClassicAbstraction;
import org.processmining.alphaminer.abstractions.AlphaRobustAbstraction;
import org.processmining.framework.plugin.PluginContext;

import au.edu.qut.pm.spn_estimator.BillClintonWeightEstimator;
import au.edu.qut.pm.spn_estimator.WeightEstimator;

public class StochasticAlphaMinerBillClintonImpl<E> extends StochasticAlphaMinerImpl<E>  {

	public StochasticAlphaMinerBillClintonImpl(AlphaClassicAbstraction<E> abstraction, PluginContext context) {
		super(abstraction, context);
	}
	
	@Override
	protected WeightEstimator createEstimator() {
		AlphaRobustAbstraction<E> abstraction = (AlphaRobustAbstraction<E>) getAbstraction();
		WeightEstimator estimator = 
				new BillClintonWeightEstimator<E>(abstraction.getDirectlyFollowsAbstraction(),
											abstraction.getRobustActivityCount(),
											abstraction.getStartActivityAbstraction(),
											 transition2class);
		return estimator;
	}
}
