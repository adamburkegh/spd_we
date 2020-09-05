package au.edu.qut.pm.spn_discover;

import org.simpleframework.xml.Root;

@Root
public enum Measure {
	LOG_EVENT_COUNT("Log Events"), LOG_TRACE_COUNT("Log Traces"), 
	MODEL_ENTITY_COUNT("Entities"), MODEL_EDGE_COUNT("Edges"), 
	NORMALIZED_PETRI_NET_EDGE_SIMPLICITY,
	EARTH_MOVERS_LIGHT_COVERAGE("tEMSC 0.8"), // Leemans 2019
	EARTH_MOVERS_SIMILARITY, 
	ENTROPY_PRECISION("S_precision"), 
	ENTROPY_RECALL("S_recall") // Leemans and Polyvyany 2020 entropy quality measures
	;
	
	private String text = null;
	
	private Measure(String text) {
		this.text = text;
	}
	
	private Measure() {
	}
	
	public String getText() {
		if (text == null) {
			String result = name().replace("_", " ");
			return result;
		}
		return text;
	}
}