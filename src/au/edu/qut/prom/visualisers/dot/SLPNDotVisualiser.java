package au.edu.qut.prom.visualisers.dot;

import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.StochasticNet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.TimedTransition;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.DotNode;
import org.processmining.plugins.graphviz.visualisation.DotPanel;

import au.edu.qut.prom.helpers.StochasticPetriNetUtils;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

public class SLPNDotVisualiser {
	
	private SLPNDotVisualiser() {};
	
	public static final String PlaceFill = "#f2f2f2";
	public static final String StartingPlaceFill = "#80ff00";
	public static final String EndingPlaceFill = "#FF3939";
	public static final String TransitionFill = "#e9c6af";
	public static final String TauFill = "#808080";
	public static final String WeightFill = "#c0bbbb";
	
	public static final DotPanel visualise(StochasticNet net) {
		Dot dot = new Dot();

		dot.setOption("forcelabels", "true");
		dot.setOption("bgcolor", "none");

		TIntObjectMap<DotNode> place2dotNode = new TIntObjectHashMap<>(10, 0.5f, -1);
		
		Marking marking = StochasticPetriNetUtils.guessInitialMarking((Petrinet) net);
		
		for (Place place: net.getPlaces()) {
			DotNode dotNode = dot.addNode("");
			dotNode.setOption("shape", "circle");
			dotNode.setOption("style", "filled");
			dotNode.setOption("fillcolor", PlaceFill);
			place2dotNode.put(place.getId().hashCode(), dotNode);

			if (marking.contains(place)) {
				dotNode.setOption("fillcolor", StartingPlaceFill);
			}
			
			if (net.getOutEdges(place).size() == 0){
				dotNode.setOption("fillcolor", EndingPlaceFill);
			}

			decoratePlace(net, place, dotNode);
		}
		
		int tau = 0;
		for (Transition trans: net.getTransitions()) {
			DotNode dotNode;
			TimedTransition ttrans = null;
			if (trans instanceof TimedTransition) {
				ttrans = (TimedTransition) trans;
			}
			if (trans.isInvisible()) {
				tau+= 1;
				dotNode = dot.addNode("<"
						+ "<TABLE"
						+ " BORDER=\"0\" "
						+ "><TR>"
						+ "<TD><FONT POINT-SIZE=\"16\" >"
						+ "&#120591;"
						+ "</FONT>"
						+ "<FONT POINT-SIZE=\"10\">(" 
						+ tau 
						+")</FONT></TD>"
						+ "</TR>"
						+ "<TR>"
						+ "<TD ALIGN=\"LEFT\">"
						+ "<FONT ALIGN=\"LEFT\" POINT-SIZE=\"10\" >"
						+ "<I>weight:</I>"
						+ "</FONT>"
						+ "</TD>"
						+ "</TR>"
						+ "<TR>"
						+ "<TD BORDER=\"1\" BGCOLOR=\"#c0bbbb\" "
						+ "STYLE=\"ROUNDED,DASHED\" "
						+ "CELLPADDING=\"5\" "
						+ ">"
						+ ttrans.getWeight()
						+ "</TD>"
						+ "</TR>"
						+ "</TABLE>"
						+ ">");
				dotNode.setOption("style", "filled,rounded");
				dotNode.setOption("fillcolor", TauFill);
			} else {
				dotNode = dot.addNode("<"
						+ "<TABLE"
						+ " BORDER=\"0\" "
						+ "><TR>"
						+ "<TD>" 
						+ trans.getLabel() 
						+"</TD>"
						+ "</TR>"
						+ "<TR>"
						+ "<TD ALIGN=\"LEFT\">"
						+ "<FONT ALIGN=\"LEFT\" POINT-SIZE=\"10\" >"
						+ "<I>weight:</I>"
						+ "</FONT>"
						+ "</TD>"
						+ "</TR>"
						+ "<TR>"
						+ "<TD BORDER=\"1\" BGCOLOR=\"#c0bbbb\" "
						+ "STYLE=\"ROUNDED,DASHED\" "
						+ "CELLPADDING=\"5\" "
						+ ">"
						+ ttrans.getWeight()
						+ "</TD>"
						+ "</TR>"
						+ "</TABLE>"
						+ ">");
				dotNode.setOption("style", "rounded,filled");
				dotNode.setOption("fillcolor", TransitionFill);
			}

			dotNode.setOption("shape", "box");
			dotNode.setOption("width", "1");

			decorateTransition(net, trans, dotNode);
			
			for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge: net.getOutEdges(trans)) {
					Place pplace = (Place) edge.getTarget();
					dot.addEdge(dotNode, 
						place2dotNode.get(pplace.getId().hashCode())
					);
			}
			for ( PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : net.getInEdges(trans)) {
					Place pplace = (Place) edge.getSource();
					dot.addEdge(
						place2dotNode.get(pplace.getId().hashCode()),
						dotNode
					);
			}
		}

		return new DotPanel(dot);
	}
	
	public static void decoratePlace(StochasticNet net, Place place, DotNode dotNode) {
		
	}

	public static void decorateTransition(StochasticNet net, Transition transition, DotNode dotNode) {
		
	}

}
