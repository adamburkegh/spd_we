package au.edu.qut.pm.stochastic;

import java.util.HashMap;
import java.util.Map;

import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.directed.DirectedGraphElement;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.TimedTransition;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.AbstractResetInhibitorNet;
import org.processmining.models.graphbased.directed.petrinet.impl.StochasticNetImpl;

public class RetraceableStochasticNetCloner extends StochasticNetImpl{

	protected Petrinet source;
	private Map<DirectedGraphElement, DirectedGraphElement> mapping;
	
	public RetraceableStochasticNetCloner(Petrinet other) {
		super(other.getLabel());
		this.source = other;
	}

	/**
	 * Does not support inhibitor, reset or weighted arcs
	 */
	public void cloneFromPetriNet() {
		mapping = cloneFrom((AbstractResetInhibitorNet)source, true, true, true, true, true);
	}

    protected synchronized Map<DirectedGraphElement, DirectedGraphElement> cloneFrom(AbstractResetInhibitorNet net,
                                                                                     boolean transitions, boolean places, boolean arcs) {
        mapping = new HashMap<DirectedGraphElement, DirectedGraphElement>();

        if (transitions) {
            for (Transition t : net.getTransitions()) {
                TimedTransition copy = addTimedTransition(t.getLabel(), DistributionType.IMMEDIATE);
                copy.setInvisible(t.isInvisible());
                mapping.put(t, copy);
            }
        }
        if (places) {
            for (Place p : net.getPlaces()) {
                Place copy = addPlace(p.getLabel());
                mapping.put(p, copy);
            }
        }
        if (arcs) {
            for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> a : net.getEdges()) {
                mapping.put(a, 
                		addArcPrivate((PetrinetNode) mapping.get(a.getSource()), (PetrinetNode) mapping.get(a
                							.getTarget()), 1, a.getParent()));
            }
        }
        getAttributeMap().clear();
        AttributeMap map = net.getAttributeMap();
        for (String key : map.keySet()) {
            getAttributeMap().put(key, map.get(key));
        }

        return mapping;
    }

    public Map<DirectedGraphElement, DirectedGraphElement> getMapping(){
    	return mapping;
    }
	
}