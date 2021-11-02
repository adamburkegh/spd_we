package au.edu.qut.pm.spn_discover;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetImpl;

import au.edu.qut.pm.spn_estimator.LogSourcedWeightEstimator;
import au.edu.qut.pm.stochastic.ProcessTreeConverter;
import au.edu.qut.pm.stochastic.StochasticNetDescriptor;
import qut.pm.spm.ppt.ProbProcessTree;
import qut.pm.spm.ppt.ProbProcessTreeFactory;
import qut.pm.spm.ppt.ProbProcessTreeFormatter;

/** 
 * 
 * Discover Process Tree -> Estimate using PN -> Reimport Weights   
 * 
 * @author burkeat
 *
 */
public class PPTEstimateRunner extends ModelRunner{

	private static final Logger LOGGER = LogManager.getLogger();
	

	private void runProcessTreeEstimation(String df) throws Exception{
		LOGGER.info("Running process tree discovery and weight estimation");
		ProbProcessTreeFactory.setStrictValidation(false);
		for (StochasticNetLogMiner miner: miners) {
			runSingleMinerRun(miner, df, df);
			for (LogSourcedWeightEstimator estimator: estimators) {
				estimator.setMinimizeCloning(true);
				if (!miner.isStochasticNetProducer()) {
					AcceptingPetriNet apn = 
							new AcceptingPetriNetImpl(miner.getStochasticNetDescriptor().getNet(),
									miner.getStochasticNetDescriptor().getInitialMarking(),
									miner.getStochasticNetDescriptor().getFinalMarkings() );
					PetrinetSource pnSource = 
							new PetrinetSource( apn,
												miner.getShortID() );
					StochasticNetDescriptor spn = runEstimator( estimator, df, df, pnSource);
					if (miner.isProcessTreeProducer()) {
						ProcessTreeConverter converter = miner.getConverter();
						ProbProcessTree ppt = converter.convertStochasticPetriNetToProbProcessTree(spn);
						LOGGER.info("\n" + new ProbProcessTreeFormatter().textTree(ppt) );
					}
				}
			}								
		}			
	}

	@Override
	public void runAll() throws Exception{
		for (String df: dataFiles) {
			runProcessTreeEstimation(df);
		}
	}

	
	public static void main(String[] args) throws Exception {
		LOGGER.info("ProbProcessTree estimator runner initializing");
		LOGGER.debug( "java.library.path=" + System.getProperty("java.library.path") );

		ModelRunner modelRunner = new PPTEstimateRunner();
		modelRunner.configure();
		modelRunner.runAll();
			
		LOGGER.info("ProbProcessTree estimator runner finished");
		System.exit(0);
	}

	
}
