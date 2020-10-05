package org.processmining.plugins.sdpwe;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetImpl;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.packages.PackageManager.Canceller;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.StochasticNet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeReduce.ReductionFailedException;
import org.processmining.plugins.InductiveMiner.efficienttree.UnknownTreeNodeException;
import org.processmining.plugins.inductiveminer2.logs.IMLog;
import org.processmining.plugins.inductiveminer2.mining.InductiveMiner;
import org.processmining.plugins.inductiveminer2.plugins.InductiveMinerPlugin;
import org.processmining.plugins.inductiveminer2.variants.MiningParametersIMInfrequent;

import au.edu.qut.pm.spn_discover.LogSourcedWeightEstimator;
import au.edu.qut.pm.spn_discover.MeanScaledActivityPairRHEstimator;
import au.edu.qut.prom.helpers.StochasticPetriNetUtils;

@Plugin(name = "Mine Stochastic Petri net with estimators", 
		level = PluginLevel.Regular, 
		returnLabels = {"GSPN" }, 
		returnTypes = { StochasticNet.class }, 
		parameterLabels = { "Log", "Petri Net"  }, 
		userAccessible = true, 
		help = SPDWeightEstimatorPlugin.TEXT)
public class SPDWeightEstimatorPlugin {

	// TODO Allow user to select classifier, estimators
	// TODO Expose other five estimators

	
	public static final String AFFILIATION = "Queensland University of Technology";
	public static final String AUTHOR = "Adam Burke, Sander Leemans, Moe Thandar Wynn";
	public static final String EMAIL = "at.burke@qut.edu.au";
	public static final String TEXT = "Produce a GSPN with immediate transitions from an input log and Petri Net control model.\n"
									+ "The algorithms implemented here are detailed in \n"
									+ "Burke, Leemans and Wynn - Stochastic Process Discovery By Weight Estimation (2020)";

	private static final String DEFAULT_MINER = "Inductive Miner";
	private static final String DEFAULT_ESTIMATOR = "Mean-Scaled RH Activity-Pair Estimator";
	
	private static XEventNameClassifier defaultClassifier() {
		return new XEventNameClassifier();
	}

	private static MeanScaledActivityPairRHEstimator defaultEstimator() {
		return new MeanScaledActivityPairRHEstimator();
	}
	
	private static String getDefaultMinerName() {
		return DEFAULT_MINER;
	}


	@UITopiaVariant(affiliation = AFFILIATION, author = AUTHOR, email = EMAIL, 
					uiLabel = "Mine Stochastic Petri net from Log with Estimator",
					uiHelp = "Use " + DEFAULT_ESTIMATOR + " and " + DEFAULT_MINER + ". " + TEXT)
	@PluginVariant(variantLabel = "Mine Stochastic Petri net from Log with Estimator and " + DEFAULT_MINER + ")", 
				   requiredParameterLabels = {0})
	public static StochasticNet mineSPNFromLogWithDefaults(final PluginContext context, XLog log) {
		try {
			return mineSPNFromLogWithEstimator(context, log, 
					defaultEstimator(), defaultClassifier());
		} catch (Exception e) {
			context.log(e);
		}
		return null;
	}

	@UITopiaVariant(affiliation = AFFILIATION, author = AUTHOR, email = EMAIL, 
					uiHelp="Mine Stochastic Petri net with selected estimator. " + TEXT)
	@PluginVariant(variantLabel = "Mine Stochastic Petri net with selected estimator." + TEXT, 
					requiredParameterLabels = {0, 1} )
	public static StochasticNet mineGUISPNWithEstimator(final UIPluginContext context, XLog log, Petrinet pnet ) {
		EstimatorPluginConfiguration estConfig = new EstimatorPluginConfiguration(log);
		InteractionResult interaction = context.showConfiguration("Configure stochastic weight estimation", estConfig );
		if (interaction != InteractionResult.CONTINUE) {
			context.getFutureResult(0).cancel(false);
			return null;
		}
		LogSourcedWeightEstimator estimator = estConfig.getEstimator();
		Marking initialMarking = StochasticPetriNetUtils.guessInitialMarking(pnet);
		AcceptingPetriNet apnet = new AcceptingPetriNetImpl(pnet, initialMarking);
		StochasticNet resultNet = mineSPNWithEstimator(context, apnet, log, estimator, defaultClassifier());
		return resultNet;
	}

	
	public static StochasticNet mineSPNWithEstimator(final PluginContext context, AcceptingPetriNet apnet, XLog log,
			LogSourcedWeightEstimator estimator, XEventClassifier classifier) 
	{
		context.log("Mining with estimator " + estimator.getReadableID() + "...");
		StochasticNet resultNet = estimator.estimateWeights(apnet, log, classifier);
		return resultNet;
	}

	
	public static StochasticNet mineSPNFromLogWithEstimator(final PluginContext context, XLog log,
			LogSourcedWeightEstimator estimator, XEventClassifier classifier)
					throws Exception
	{
		context.log("Mining control flow from log with " + getDefaultMinerName());
		AcceptingPetriNet apnet = mineWithDefaultMiner(context, log, classifier);
		context.log("Mining with estimator " + estimator.getReadableID() + "...");
		StochasticNet resultNet = estimator.estimateWeights(apnet, log, classifier);
		return resultNet;
	}


	public static AcceptingPetriNet mineWithDefaultMiner(final PluginContext context, XLog log, XEventClassifier classifier) 
			throws UnknownTreeNodeException, ReductionFailedException 
	{
		context.log("Using classifier " + classifier.getClass());
		MiningParametersIMInfrequent parameters = new MiningParametersIMInfrequent();
		parameters.setClassifier(classifier); 
		IMLog imlog = parameters.getIMLog(log);
		context.log("Starting inductive miner ...");
		EfficientTree tree = InductiveMiner.mineEfficientTree(imlog, parameters, new Canceller() {
			public boolean isCancelled() {
				return context.getProgress().isCancelled();
			}
		});
		AcceptingPetriNet pnet = InductiveMinerPlugin.postProcessTree2PetriNet(tree, new Canceller() {
					public boolean isCancelled() {
						return context.getProgress().isCancelled();
					}
				});
		return pnet;
	}
	
}
