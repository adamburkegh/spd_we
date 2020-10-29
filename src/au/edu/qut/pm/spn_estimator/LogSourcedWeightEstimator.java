package au.edu.qut.pm.spn_estimator;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.models.graphbased.directed.petrinet.StochasticNet;

import au.edu.qut.pm.stochastic.ArtifactCreator;

public interface LogSourcedWeightEstimator extends WeightEstimator, ArtifactCreator{

	public StochasticNet estimateWeights(AcceptingPetriNet net, XLog log, XEventClassifier classifier);
	
}
