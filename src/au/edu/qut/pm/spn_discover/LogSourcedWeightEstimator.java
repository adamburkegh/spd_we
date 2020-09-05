package au.edu.qut.pm.spn_discover;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.models.graphbased.directed.petrinet.StochasticNet;

public interface LogSourcedWeightEstimator extends WeightEstimator, ArtifactCreator{

	public StochasticNet estimateWeights(AcceptingPetriNet net, XLog log, XEventClassifier classifier);
	
}
