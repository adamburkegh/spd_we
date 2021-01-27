package au.edu.qut.pm.spn_discover;

import java.util.Set;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.models.graphbased.directed.petrinet.StochasticNet;
import org.processmining.models.semantics.petrinet.Marking;

import au.edu.qut.pm.spn_estimator.LogSourcedWeightEstimator;
import au.edu.qut.pm.stochastic.StochasticNetDescriptor;
import au.edu.qut.prom.helpers.StochasticPetriNetUtils;

public class EstimatorRun implements StochasticArtifactRun{

	private LogSourcedWeightEstimator estimator;
	private String modelName;
	private StochasticNetDescriptor result; 
	
	public EstimatorRun(LogSourcedWeightEstimator estimator, String modelName) {
		this.estimator = estimator;
		this.modelName = modelName;
	}
	
	@Override
	public String getShortID() {
		return estimator.getShortID() + "-" + modelName;
	}

	@Override
	public String getReadableID() {
		return estimator.getReadableID();
	}

	@Override
	public StochasticNetDescriptor getStochasticNetDescriptor() {
		return result;
	}

	public StochasticNet estimateWeights(AcceptingPetriNet net, XLog log, XEventClassifier classifier) {
		StochasticNet snet = estimator.estimateWeights(net, log, classifier);
		Marking initialMarking = StochasticPetriNetUtils.findEquivalentInitialMarking( net.getInitialMarking(), snet );
		Set<Marking> finalMarkings = StochasticPetriNetUtils.findEquivalentFinalMarkings( net.getFinalMarkings(), snet );
		result = new StochasticNetDescriptor(snet.getLabel(), snet,initialMarking, finalMarkings);
		return snet;
	}
}
