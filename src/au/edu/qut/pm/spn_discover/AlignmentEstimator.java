package au.edu.qut.pm.spn_discover;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.StochasticNet;
import org.processmining.models.graphbased.directed.petrinet.StochasticNet.DistributionType;
import org.processmining.models.graphbased.directed.petrinet.StochasticNet.ExecutionPolicy;
import org.processmining.models.graphbased.directed.petrinet.StochasticNet.TimeUnit;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.StochasticNetImpl;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.astar.petrinet.PetrinetReplayerWithILP;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.inductiveVisualMiner.alignment.AcceptingPetriNetAlignment;
import org.processmining.plugins.inductiveVisualMiner.alignment.IvMEventClasses;
import org.processmining.plugins.petrinet.replayer.PNLogReplayer;
import org.processmining.plugins.petrinet.replayer.algorithms.IPNReplayAlgorithm;
import org.processmining.plugins.petrinet.replayer.algorithms.costbasedcomplete.CostBasedCompleteParam;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.petrinet.replayresult.StepTypes;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;

import au.edu.qut.prom.helpers.StochasticPetriNetUtils;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import nl.tue.astar.AStarException;

public class AlignmentEstimator implements LogSourcedWeightEstimator {

	private static Logger LOGGER = LogManager.getLogger();
	
	@Override
	public void estimateWeights(StochasticNet net) {
		throw new RuntimeException(
				"Indirect estimation not supported. Use estimateWeights(net,log,classifier)");
	}

	@Override
	public String getShortID() {
		return "align";
	}

	@Override
	public String getReadableID() {
		return "Alignment";
	}

	@Override
	public StochasticNet estimateWeights(AcceptingPetriNet inputNet, XLog log, XEventClassifier classifier) {
		XLogInfo xLogInfo = XLogInfoFactory.createLogInfo(log, classifier);
		IvMEventClasses eventClasses = new IvMEventClasses(xLogInfo.getEventClasses());
		checkAndDefaultMarkings(inputNet);
		AcceptingPetriNetAlignment.addAllLeavesAsPerformanceEventClasses(eventClasses, inputNet);

		TObjectIntMap<Transition> transition2occurrence = new TObjectIntHashMap<Transition>(10, 0.5f, 0);

		XEventClass dummy = new XEventClass("", 1);
		TransEvClassMapping mapping = createTransitionEventClassMapping(inputNet, eventClasses, dummy);

		PNLogReplayer replayer = new PNLogReplayer();
		CostBasedCompleteParam replayParameters = new CostBasedCompleteParam(eventClasses.getClasses(), dummy,
				inputNet.getNet().getTransitions(), 1, 1);
		replayParameters.setInitialMarking(inputNet.getInitialMarking());
		replayParameters.setMaxNumOfStates(Integer.MAX_VALUE);
		IPNReplayAlgorithm algorithm = new PetrinetReplayerWithILP();
		Marking[] finalMarkings = new Marking[inputNet.getFinalMarkings().size()];
		replayParameters.setFinalMarkings(inputNet.getFinalMarkings().toArray(finalMarkings));
		replayParameters.setCreateConn(false);
		replayParameters.setGUIMode(false);

		PNRepResult replayResult = replayLog(inputNet, log, mapping, replayer, replayParameters, algorithm);

		if (replayResult == null) {
			LOGGER.error("Couldn't calculate alignment for {}", inputNet);
			throw new RuntimeException("Couldn't calculate alignment for input net");			
		}
		
		for (SyncReplayResult aTrace : replayResult) {
			for (@SuppressWarnings("unused") Integer traceIndex : aTrace.getTraceIndex()) {
				Iterator<StepTypes> itType = aTrace.getStepTypes().iterator();
				Iterator<Object> itNode = aTrace.getNodeInstance().iterator();
				while (itType.hasNext()) {
					StepTypes type = itType.next();
					Object node = itNode.next();
					if (type == StepTypes.MREAL || type == StepTypes.LMGOOD) {
						if (!(node instanceof Transition)){
							LOGGER.error("Node {} wasn't a transition",node);
							throw new RuntimeException("Node wasn't a transition" + node.toString());
						}
						transition2occurrence.adjustOrPutValue((Transition) node, 1, 1);
					}
				}
			}
		}
		StochasticNet result = copyNet(inputNet, transition2occurrence);
		return result;	
	}

	private void checkAndDefaultMarkings(AcceptingPetriNet inputNet) {
		if (inputNet.getInitialMarking().isEmpty() ) {
			LOGGER.info("Initial markings were empty - guessing");
			Marking initialMarking = StochasticPetriNetUtils.guessInitialMarking(inputNet.getNet());
			if (initialMarking.isEmpty()) {
				LOGGER.error("Initial markings required for alignment calculation for -  {}", 
						inputNet);
				throw new RuntimeException("Initial markings not supplied for alignment calculation and couldn't guess");
			}
			inputNet.setInitialMarking(initialMarking);
		}
		if (inputNet.getFinalMarkings().isEmpty()
				|| (null == inputNet.getFinalMarkings().iterator().next() ) ) 
		{
			LOGGER.info("Final markings were empty - using guessed final places");
			Set<Marking> guessedFinalMarkings = 
					StochasticPetriNetUtils.guessFinalMarkingsAsIfJustFinalPlaces(inputNet.getNet());
			if (guessedFinalMarkings.isEmpty()) {
				LOGGER.error("Couldn't guess final markings");
				throw new RuntimeException("Final markings not supplied for alignment calculation and couldn't guess");
			}
			inputNet.setFinalMarkings(guessedFinalMarkings);
		}
	}

	private PNRepResult replayLog(AcceptingPetriNet inputNet, XLog log, TransEvClassMapping mapping,
			PNLogReplayer replayer, CostBasedCompleteParam replayParameters, IPNReplayAlgorithm algorithm) {
		PNRepResult replayResult = null;
		try {
			replayResult = replayer.replayLog(null, inputNet.getNet(), log, mapping, algorithm,
					replayParameters);
		}catch(AStarException ase) {
			LOGGER.error("Error during log replay",ase);
			throw new RuntimeException(ase.getMessage());
		}
		return replayResult;
	}

	private TransEvClassMapping createTransitionEventClassMapping(AcceptingPetriNet inputNet,
			IvMEventClasses eventClasses, XEventClass dummy) {
		TransEvClassMapping mapping;
		{
			mapping = new TransEvClassMapping(eventClasses.getClassifier(), dummy);
			for (Transition t : inputNet.getNet().getTransitions()) {
				if (t.isInvisible()) {
					mapping.put(t, dummy);
				} else {
					mapping.put(t, eventClasses.getByIdentity(t.getLabel()));
				}
			}
		}
		return mapping;
	}

	private StochasticNet copyNet(AcceptingPetriNet inputNet, TObjectIntMap<Transition> transition2occurrence) {
		StochasticNet result = new StochasticNetImpl(inputNet.getNet().getLabel());
		result.setExecutionPolicy(ExecutionPolicy.RACE_ENABLING_MEMORY);
		result.setTimeUnit(TimeUnit.HOURS);
		Map<PetrinetNode, PetrinetNode> input2result = new THashMap<>();
		for (Place inputPlace : inputNet.getNet().getPlaces()) {
			Place resultPlace = result.addPlace(inputPlace.getLabel());
			input2result.put(inputPlace, resultPlace);
		}

		for (Transition inputTransition : inputNet.getNet().getTransitions()) {
			Transition resultTransition = result.addTimedTransition(inputTransition.getLabel(),
					transition2occurrence.get(inputTransition), DistributionType.UNIFORM, 0.0, 200.0);

			resultTransition.setInvisible(inputTransition.isInvisible());
			input2result.put(inputTransition, resultTransition);
		}

		for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : inputNet.getNet().getEdges()) {
			PetrinetNode resultSource = input2result.get(edge.getSource());
			PetrinetNode resultTarget = input2result.get(edge.getTarget());
			if (resultSource instanceof Place) {
				result.addArc((Place) resultSource, (Transition) resultTarget);
			} else {
				result.addArc((Transition) resultSource, (Place) resultTarget);
			}
		}
		return result;
	}

}
