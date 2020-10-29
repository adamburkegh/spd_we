package au.edu.qut.pm.spn_discover;

import au.edu.qut.pm.stochastic.ArtifactCreator;
import au.edu.qut.pm.stochastic.StochasticNetDescriptor;

public interface StochasticArtifactRun extends ArtifactCreator{
	public StochasticNetDescriptor getStochasticNetDescriptor();
}
