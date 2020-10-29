package au.edu.qut.pm.spn_discover;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.StochasticNet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.TimedTransition;
import org.processmining.models.graphbased.directed.petrinet.impl.StochasticNetImpl;
import org.processmining.models.semantics.petrinet.Marking;

import au.edu.qut.pm.stochastic.StochasticNetDescriptor;
import au.edu.qut.xes.helpers.XESLogUtils;


/**
 * Lab tinkering with an algo that creates a frequency-annotated decision tree from log traces, 
 * by working from the head of the trace. It squeezes together the traces from the head end,
 * like squeezing a toothpaste tube.
 * 
 * Super-naive. No loops, blatantly exploits finite nature of log. Results in a monster Petri Net
 * that can not even be visualized for many BPIC logs, as long traces, concurrency and loops get 
 * ignored. 
 * 
 * Uses a GSPN to represent the decision tree but ignores timing, just to see what happens
 * 
 * Not even any tests atm
 * 
 * @author burkeat
 *
 */
public class ToothpasteTreeMiner implements StochasticNetLogMiner{

	private static final String XES_CONCEPT_NAME = "concept:name";
	private static final String FINAL = "FINAL";
	private static final String INITIAL = "INITIAL";
	private static Logger LOGGER = LogManager.getLogger();
	private StochasticNet net;
	private int placeCount;
	private XEventClassifier classifier;
	
	private static class Node{
		private String label;
		private int siblingRelativeEventCount = 0;
		private int childTotalEventCount = 0;
		private double siblingRelativeProbability = 0; // only calculated at end
		private Map<String,Node> children = new HashMap<String,Node>();
		
		public Node(String label) {
			this.label = label;
		}

		public Node newObservedEvent(String eventLabel) {
			Node existing = children.get(eventLabel);
			if (existing == null) {
				Node newNode = new Node(eventLabel);
				children.put(eventLabel, newNode);
				existing = newNode;
			}
			existing.siblingRelativeEventCount += 1;
			childTotalEventCount += 1;
			return existing;
		}
		
		public void calculateProbabilities() {
			this.siblingRelativeProbability = 1.0d;
			calculateChildProbabilities();
		}

		private void calculateChildProbabilities() {
			for (String label: children.keySet()) {
				Node child = children.get(label);
				child.siblingRelativeProbability = 
						(double)child.siblingRelativeEventCount / (double)childTotalEventCount;
				child.calculateChildProbabilities();
			}
		}
		

		/**
		 * Note only considers label
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((label == null) ? 0 : label.hashCode());
			return result;
		}

		/**
		 * Note only considers label
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Node other = (Node) obj;
			if (label == null) {
				if (other.label != null)
					return false;
			} else if (!label.equals(other.label))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "Node [label=" + label + ", siblingRelativeEventCount=" + siblingRelativeEventCount
					+ ", childTotalEventCount=" + childTotalEventCount + ", siblingRelativeProbability="
					+ siblingRelativeProbability + " #children=" + children.size() + "]";
		}

		public String formatAsTree() {
			return "\n" + formatAsTree(0);
		}
		
		private String formatAsTree(int offset) {
			StringBuffer result = new StringBuffer("");
			for (int i = 0; i < offset; i++) {
				result.append(" - ");
			}
			result.append( "Node [label = " + label + ", "
					+ "siblingRelativeEventCount=" + siblingRelativeEventCount 
					+ ", siblingRelativeProbability= " + siblingRelativeProbability + "]\n" );
			for (String childLabel: children.keySet()) {
				result.append( 
						children.get(childLabel).formatAsTree(offset+1) );
			}
			return result.toString();			
		}
		
	}
	
	@Override
	public String getShortID() {
		return "tm";
	}
	
	@Override
	public String getReadableID() {
		return "ToothpasteMiner";
	}
	
	public void run(PluginContext uipc, XLog log, File outputModelFile) throws Exception{
		reset();
		classifier = XESLogUtils.detectNameBasedClassifier(log);
		LOGGER.debug("Using classifier {}", classifier);
		Node decisionTree = buildDecisionTree(log);
		LOGGER.info("Found decision tree: {}", decisionTree);
		net = transformDecisonTreeToStochasticNet(decisionTree, log.getAttributes().get(XES_CONCEPT_NAME).toString());
		LOGGER.info("Transformed to SPN: {}", net);
	}

	private void reset() {
		placeCount = 0;
	}

	private StochasticNet transformDecisonTreeToStochasticNet(Node decisionTree, String name) {
		StochasticNet result = new StochasticNetImpl(name);
		Place initialPlace 	= result.addPlace(INITIAL);
		Place finalPlace 	= result.addPlace(FINAL);
		transformSubtree(decisionTree, result, initialPlace, finalPlace);
		squeezeFromFinal(result, finalPlace);
		return result;
	}

	private void squeezeFromFinal(StochasticNet result, Place finalPlace) {
		// All synonymous transitions coming into FINAL which have probability of 1.0
		// can be consolidated
		// Formal proof pending, but it totally works honest guv
		LOGGER.debug("squeezeFromFinal()");
		Map<String,TimedTransition> incoming = new HashMap<String,TimedTransition>();
		for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge: result.getInEdges(finalPlace)){
			String incomingTransitionLabel = edge.getSource().getLabel();
			TimedTransition sourceTransition = (TimedTransition)edge.getSource();
			Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> inEdges = 
					sourceTransition.getGraph().getInEdges(sourceTransition);
			if (inEdges.size() == 1) {
				// ie, only if probability == 1.0
				LOGGER.debug("Found 1.0 chain {} :: {}",incomingTransitionLabel,sourceTransition.getId());
				PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> placeEdge = 
						inEdges.iterator().next();
				// Downcast is bad, but don't see another way to do it
				Place sourcePlace = (Place) placeEdge.getSource(); 
				if (incoming.containsKey(incomingTransitionLabel )) {
					TimedTransition existing = incoming.get(incomingTransitionLabel);
					LOGGER.debug("Replacing {} (from {}) with edge from {} to {} ({})", edge, edge.getSource(), sourcePlace, 
							existing, existing.getId());
					result.removeEdge(edge);
					result.removeTransition(sourceTransition);
					result.addArc(sourcePlace, existing);
				}else {
					LOGGER.debug("Tracking new transition {} from {} ({})", incomingTransitionLabel, 
							sourceTransition, sourceTransition.getId());
					incoming.put(incomingTransitionLabel, sourceTransition);
				}
				// Could recurse here, but it's a tree - so nothing to do, because never 
				// multiple incoming arcs
			}
		}
		
	}

	private void transformSubtree(Node decisionTree, StochasticNet resultNet, Place lastPlace, 
									Place finalPlace) 
	{
		for (String childLabel: decisionTree.children.keySet()) {
			Node child = decisionTree.children.get(childLabel);
			TimedTransition transition = resultNet.addImmediateTransition(childLabel);
			transition.setImmediate(true);
			transition.setWeight(child.siblingRelativeEventCount);
			resultNet.addArc(lastPlace, transition);
			if (child.children.size() > 0) {
				Place landingPlace = resultNet.addPlace(newPlaceLabel());
				resultNet.addArc(transition, landingPlace);
				transformSubtree(child,resultNet,landingPlace,finalPlace);
			}else {
				resultNet.addArc(transition, finalPlace);
			}
		}
	}

	private String newPlaceLabel() {
		String label = "p" + placeCount;
		placeCount++;
		return label;
	}
	
	

	private Node buildDecisionTree(XLog log) {
		// This wants to be recursive, but Java 8 and XES make it a little awkward
		// Seems like the XLog / XTrace should already hold frequency info, but I don't see it
		Node root = new Node(INITIAL);
		for (XTrace trace: log) {
			LOGGER.debug(root);
			addTraceToTree(root, trace);
		}
		root.calculateProbabilities();
		LOGGER.debug(root.formatAsTree());
		return root;
	}
	

	private void addTraceToTree(Node parent, XTrace trace) {
		LOGGER.debug("addTraceToTree {} {}",parent, parent.children);
		Node current = parent;
		Node nextChild = null;
		for (XEvent event: trace) {
			String label = event.getAttributes().get(XES_CONCEPT_NAME).toString();
			// Not sure how general concept:name actually is as a key in XES
			nextChild = current.newObservedEvent(label);
			current = nextChild;
		}
	}

	@Override
	public StochasticNetDescriptor getStochasticNetDescriptor() {
		return new StochasticNetDescriptor(net.getLabel(),net, new Marking());
	}
	
}
