package au.edu.qut.pm.ars.spn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.StochasticNet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.mining.MiningParametersIMf;
import org.processmining.plugins.InductiveMiner.plugins.IMPetriNet;
import org.processmining.plugins.petrinet.manifestreplayresult.Manifest;
import org.processmining.plugins.stochasticpetrinet.StochasticNetUtils;
import org.processmining.plugins.stochasticpetrinet.enricher.PerformanceEnricherConfig;
import org.processmining.plugins.stochasticpetrinet.enricher.PerformanceEnricherPlugin;

/**
 * Copy by @author burkeat for purpose of more detailed analysis - add tracing.
 * 
 * Original class org.processmining.plugins.stochasticpetrinet.miner.StochasticMinerPlugin in
 * StochasticPetriNets PROM package, found at revision 33973 from 10/02/17 in
 * https://svn.win.tue.nl/repos/prom/Packages/StochasticPetriNets/Trunk/src/org/processmining/plugins/stochasticpetrinet/miner/StochasticMinerPlugin.java
 * 
 * Code and comments preserved as is from original (including the part where it claims not to be
 * implemented yet)
 * 
 * ---
 * Not yet implemented. Use implementation based on Process Trees for now.
 * See GeneralizedMinerPlugin in "GeneralizedConformance".
 *
 * Original author Andreas Rogge-Solti
 */
public class StochasticMinerPlugin {
	
	private static Logger LOGGER = LogManager.getLogger();

//	@Plugin(name = "Mine optimal model and log", 
//			parameterLabels = { "Log", "Petri Net"}, 
//			returnLabels = { "Log", "Petri Net" }, 
//			returnTypes = { XLog.class, PetrinetGraph.class }, 
//			userAccessible = true,
//			help = "Tries to seek for altered combination of a log and model pair that best fits each other and are close to the given model and log.")
//
//	@UITopiaVariant(affiliation = "Vienna University of Economics and Business", author = "A. Rogge-Solti", email = "andreas.rogge-solti@wu.ac.at", uiLabel = UITopiaVariant.USEPLUGIN)
//	public static Object[] mineLogAndModel(PluginContext context, XLog log, PetrinetGraph model){
//		Object[] obj = new Object[2];
//		
//		DistanceFunction distFun = new SimpleDistanceFunction(); 
//		
//		OptimalMiner miner = new LocalSearchMiner(distFun, context, log, model);
//		miner.searchForBetterLogAndModel();
//		obj[0] = miner.getBestLog();
//		obj[1] = miner.getBestModel();
//		return obj;
//	}

    @Plugin(name = "Mine stochastic Petri net from log",
            parameterLabels = {"Log"},
            returnLabels = {StochasticNet.PARAMETER_LABEL, "Marking"},
            returnTypes = {StochasticNet.class, Marking.class},
            userAccessible = true,
            help = "Discovers a fitting Petri net with inductive miner and enriches it with stochastic information (time information required in the log).)."
    )
    @UITopiaVariant(affiliation = "Vienna University of Economics and Business", author = "A. Solti", email = "andreas.rogge-solti@wu.ac.at", uiLabel = UITopiaVariant.USEPLUGIN)
    public static Object[] discoverStochNetMode(UIPluginContext context, XLog log) {
    	LOGGER.info("Discovering Stochastic Petri Net");
        Petrinet net = getFittingPetrinetWithChoicesModeledAsImmediateTransitions(context, log);
        LOGGER.debug("Transforming to SPN and replaying log a second time with PerformanceEnricher");
        return PerformanceEnricherPlugin.transform(context, net, log);
    }

    public static Object[] discoverStochNetModel(UIPluginContext context, XLog log) {
    	LOGGER.info("Discovering Stochastic Petri Net");
        Petrinet net = getFittingPetrinetWithChoicesModeledAsImmediateTransitions(context, log);
        LOGGER.debug("Transforming control flow petri net to GDT_SPN (gaussian) with second log replay");
        PerformanceEnricherConfig config = new PerformanceEnricherConfig(StochasticNet.DistributionType.GAUSSIAN_KERNEL, StochasticNet.TimeUnit.HOURS, StochasticNet.ExecutionPolicy.RACE_ENABLING_MEMORY, null);
        Manifest manifest = (Manifest) StochasticNetUtils.replayLog(context, net, log, true, true);
        LOGGER.debug("Transforming to SPN and replaying log a third time with PerformanceEnricher");
        return PerformanceEnricherPlugin.transform(context, manifest, config);
    }

    public static Petrinet getFittingPetrinetWithChoicesModeledAsImmediateTransitions(UIPluginContext context, XLog log) {
        MiningParameters params = new MiningParametersIMf();
        params.setNoiseThreshold(0.0f); // to guarantee perfect fitness
        LOGGER.debug("Mining control flow with inductive minor zero noise tolerance");
        Object[] result = IMPetriNet.minePetriNet(context, log, params);
        LOGGER.debug("Transforming immediate transitions in discovered net");
        // the model is the first parameter, second is initial marking, third is final marking
        Petrinet net = (Petrinet) result[0];

        int newPlaceCount = 0;

        ArrayList<Place> places = new ArrayList<>(net.getPlaces());

        // alter structure of net: We need to represent all choices by conflicting "immediate" transitions
        for (Place place : places) {
            // check all places for outgoing arcs and see whether their transitions are visible or invisible
            Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> edges = net.getOutEdges(place);
            boolean inConflict = edges.size() > 1;
            if (inConflict) {
                // check whether we have mixed transition types:
                Set<Transition> immediateTransitions = new HashSet<>();
                Set<Transition> visibleTransitions = new HashSet<>();

                for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : edges) {
                    Transition transition = (Transition) edge.getTarget();
                    if (transition.isInvisible()) {
                        immediateTransitions.add(transition);
                    } else {
                        visibleTransitions.add(transition);
                    }
                }
                if (visibleTransitions.size() > 0) {
                    // add immediate transitions before the visible ones to allow for a probabilistic decision
                    // even when using race condition semantics.
                    for (Transition t : visibleTransitions) {
                        net.removeArc(place, t);
                        // this operation should be soundness preserving, as it is one of inverted Murata's reduction rules
                        Place choicePlace = net.addPlace("newPlace" + newPlaceCount);
                        Transition choiceTransition = net.addTransition("tau choice" + newPlaceCount++);
                        choiceTransition.setInvisible(true);

                        net.addArc(place, choiceTransition);
                        net.addArc(choiceTransition, choicePlace);
                        net.addArc(choicePlace, t);
                    }
                }
            }
        }
        return net;
    }
}
