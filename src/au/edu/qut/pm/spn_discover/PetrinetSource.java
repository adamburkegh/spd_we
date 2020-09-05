package au.edu.qut.pm.spn_discover;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;

public class PetrinetSource {

	private AcceptingPetriNet net;
	private String sourceId; // eg a filename
	
	public PetrinetSource(AcceptingPetriNet source, String sourceId) {
		super();
		this.net = source;
		this.sourceId = sourceId;
	}
	
	public Petrinet getNet() {
		return net.getNet();
	}
	
	public String getSourceId() {
		return sourceId;
	}
	
	public AcceptingPetriNet getAcceptingPetriNet() {
		return net;
	}
	
}
