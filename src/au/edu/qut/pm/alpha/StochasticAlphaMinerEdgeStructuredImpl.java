package au.edu.qut.pm.alpha;

import org.processmining.alphaminer.abstractions.AlphaRobustAbstraction;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.logabstractions.models.ColumnAbstraction;

import au.edu.qut.pm.spn_discover.EdgeStructuredEstimator;
import au.edu.qut.pm.spn_discover.WeightEstimator;

public class StochasticAlphaMinerEdgeStructuredImpl<E> extends StochasticAlphaMinerImpl<E>{

	public StochasticAlphaMinerEdgeStructuredImpl(AlphaRobustAbstraction<E> abstraction, PluginContext context) {
		super(abstraction, context);
	}

	@Override
	protected WeightEstimator createEstimator() {
		AlphaRobustAbstraction<E> abstraction = (AlphaRobustAbstraction<E>) getAbstraction();
		ColumnAbstraction<E> frequency = abstraction.getRobustActivityCount();
		WeightEstimator estimator = new EdgeStructuredEstimator<E>(frequency,transition2class);
		return estimator;
	}


}
