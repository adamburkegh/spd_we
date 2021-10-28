package au.edu.qut.pm.spn_discover;

import java.io.File;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.framework.packages.PackageManager.Canceller;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.plugins.inductiveminer2.logs.IMLog;
import org.processmining.plugins.inductiveminer2.mining.InductiveMiner;
import org.processmining.plugins.inductiveminer2.variants.MiningParametersIMInfrequent;

import au.edu.qut.pm.stochastic.ProcessTreeConverter;
import au.edu.qut.pm.stochastic.RetraceableStochasticNetCloner;
import au.edu.qut.pm.stochastic.StochasticNetDescriptor;
import au.edu.qut.prom.helpers.StochasticPetriNetUtils;

public class RetraceableControlInductiveMiner implements StochasticNetLogMiner {

	private static final Logger LOGGER = LogManager.getLogger();
	
	private StochasticNetDescriptor result = null;

	private ProcessTreeConverter converter;
	
	@Override
	public String getShortID() {
		return "ret-inductive";
	}
	
	@Override
	public String getReadableID() {
		return "Retraceable Inductive Miner";
	}

	@Override
	public void run(PluginContext uipc, XLog log, File outputModelFile) throws Exception {
		XEventClassifier targetClassifier = new XEventNameClassifier();
		LOGGER.debug("Using classifier {}", targetClassifier);
		MiningParametersIMInfrequent parameters = new MiningParametersIMInfrequent();
		parameters.setClassifier(targetClassifier); 
		IMLog imlog = parameters.getIMLog(log);
		LOGGER.debug("Starting inductive miner ...");
		EfficientTree tree = InductiveMiner.mineEfficientTree(imlog, parameters, new Canceller() {
			public boolean isCancelled() {
				return uipc.getProgress().isCancelled();
			}
		});
		converter = new ProcessTreeConverter();
		AcceptingPetriNet pnet = converter.convertProcessTreeToPetriNet(tree);
		RetraceableStochasticNetCloner net = new RetraceableStochasticNetCloner(pnet.getNet());
		net.cloneFromPetriNet();
		converter.setNet(net);
		///
		Marking initialMarking = StochasticPetriNetUtils.findEquivalentInitialMarking( pnet.getInitialMarking(), net );
		Set<Marking> finalMarkings = StochasticPetriNetUtils.findEquivalentFinalMarkings( pnet.getFinalMarkings(), net );
		result = new StochasticNetDescriptor(net.getLabel(), net, initialMarking, 
				finalMarkings);
	}


	@Override
	public StochasticNetDescriptor getStochasticNetDescriptor() {
		return result;
	}
	
	@Override
	public boolean isStochasticNetProducer() {
		return false;
	}

	@Override
	public ProcessTreeConverter getConverter() {
		return converter;
	}
	public boolean isProcessTreeProducer() {
		return true;
	}

		
}
