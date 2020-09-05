package au.edu.qut.prom.helpers;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetImpl;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.StochasticNet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.StochasticNetImpl;
import org.processmining.models.semantics.petrinet.Marking;

/**
 * Allows the creation of Petri nets with short one line ascii sketches, for example
 * <code>initialPlace -> [transition1] -> mp -> [transition2] -> finalPlace</code> 
 * 
 * Larger nets can be created with multiple invocations. Existing nodes will be looked up by 
 * label. 
 * 
 * Weighted transitions without weights, as in <code>{b}</code>, are defaulted to weight 1.0.
 * 
 * Current limitations: no support for SPNs beyond weighted transitions. No support for separate
 * nodes with duplicate labels.
 * 
 * Methods for creating {@code AcceptingPetriNets} are also provided. These use naming 
 * conventions to identify initial and final markings per 
 * {@link #createAcceptingNet(String, String)}.
 * 
 * Grammar
 * 
 * <pre>
 * PETRI_ONELINE_NET 	:: PLACE EDGE TRANSITION EDGE PLACE_LED_SUBNET
 * PLACE_LED_SUBNET  	:: PLACE EDGE TRANSITION EDGE PLACE_LED_SUBNET
 * PLACE_LED_SUBNET  	:: PLACE 
 * TRANSITION_SUBNET 	:: TRANSITION EDGE PLACE EDGE TRANSITION_SUBNET
 * TRANSITION_SUBNET 	:: TRANSITION 
 * TRANSITION        	:: SIMPLE_TRANSITION || WEIGHTED_TRANSITION
 * SIMPLE_TRANSITION 	:: '[' LABEL ']'
 * WEIGHTED_TRANSITION  :: WEIGHTED_TRAN_VALUE | WEIGHTED_TRAN_DEFAULT
 * WEIGHTED_TRAN_VALUE  :: '{' LABEL WEIGHT '}'
 * WEIGHTED_TRAN_DEFAULT:: '{' LABEL '}'
 * WEIGHT			 	:: [0-9].[0-9]*
 * PLACE             	:: LABEL
 * EDGE              	:: '->' 
 * LABEL             	:: alphanumeric string
 * </pre>
 *
 * Doesn't work for extended codepoints (eg UTF-16).
 * 
 * @param netText
 * @return
 */
public class PetriNetFragmentParser{

	private static enum TokenInfo{
		SIMPLE_TRANSITION("\\[[a-zA-Z][a-zA-Z0-9]*\\]"),
		WEIGHTED_DEFAULT_TRANSITION("\\{[a-zA-Z][a-zA-Z0-9]*\\}"),
		WEIGHTED_VALUE_TRANSITION("\\{[a-zA-Z][a-zA-Z0-9]*\\s[0-9]*\\.[0-9]*\\}"),
		EDGE("->"),
		PLACE("[a-zA-Z][a-zA-Z0-9]*"),
		TERMINAL("");

		public static final TokenInfo[] LEX_VALUES = 
				{SIMPLE_TRANSITION,WEIGHTED_DEFAULT_TRANSITION,WEIGHTED_VALUE_TRANSITION,EDGE,PLACE}; 
		
		private Pattern pattern;
		
		private TokenInfo(String regex){
			this.pattern = Pattern.compile("^\\s*("+regex+")");
		}
		
		
	}
	
	private static class Token{
		public final PetriNetFragmentParser.TokenInfo tokenInfo;
		public final String sequence;
		
		public Token(PetriNetFragmentParser.TokenInfo token, String sequence) {
			this.tokenInfo = token;
			this.sequence = sequence;
		}
		
		public String toString() {
			return sequence + ":" + tokenInfo;
		}
	}
	
	public static final Set<String> INITIAL_PLACE_LABELS;
	public static final Set<String> FINAL_PLACE_LABELS;
	
	static {
		INITIAL_PLACE_LABELS = new TreeSet<>();
		INITIAL_PLACE_LABELS.add("Start");
		INITIAL_PLACE_LABELS.add("Initial");
		INITIAL_PLACE_LABELS.add("I");
		FINAL_PLACE_LABELS = new TreeSet<>();
		FINAL_PLACE_LABELS.add("End");
		FINAL_PLACE_LABELS.add("Final");
		FINAL_PLACE_LABELS.add("F");
	}

	
	private LinkedList<Token> tokens = new LinkedList<Token>();
	private Token lookahead = null;
	private StochasticNet net;
	private Map<String,PetrinetNode> nodeLookup = new HashMap<>();
	
	public void addToNet(StochasticNet net, String netText) {
		tokenize(netText);
		this.net = net;
		parse();
	}
	
	public StochasticNet createNet(String label, String netText) {
		StochasticNet net = new StochasticNetImpl(label);
		nodeLookup = new HashMap<>();
		addToNet(net,netText);
		return net;
	}

	/**
	 * 
	 * Returns an AcceptingPetriNet with one initial and one final place marked. Initial and final
	 * markings are determined by labeling convention but will only be applied where places have the
	 * correct edge properties, ie, only outgoing for initial places, only incoming for final.
	 * 
	 *  Naming conventions for initial places, in order of checking: Start, Initial, I.
	 *  
	 *  Naming conventions for final places, in order of checking: End, Final, F.
	 * 
	 * @param label
	 * @param netText
	 * @return
	 */
	public AcceptingPetriNet createAcceptingNet(String label, String netText) {
		StochasticNet net = new StochasticNetImpl(label);
		nodeLookup = new HashMap<>();
		addToNet(net,netText);
		return markInitialFinalPlaces(net);
	}

	
	public AcceptingPetriNet markInitialFinalPlaces(StochasticNet net) {
		Set<Place> initialCandidates = new TreeSet<>();
		Set<Place> finalCandidates = new TreeSet<>();
		for (Place place: net.getPlaces()) {
			 if ( INITIAL_PLACE_LABELS.contains(place.getLabel()) 
					 && net.getInEdges(place).isEmpty() ) 
			 {
				 initialCandidates.add(place);
			 }else {
				 if (FINAL_PLACE_LABELS.contains(place.getLabel())
						 && net.getOutEdges(place).isEmpty()) 
				 {
					 finalCandidates.add(place);
				 }
			 }
		}
		Marking initialMarking = markPlaceFromCandidates(initialCandidates, INITIAL_PLACE_LABELS);
		Marking finalMarking = markPlaceFromCandidates(finalCandidates, FINAL_PLACE_LABELS);
		return new AcceptingPetriNetImpl(net,initialMarking,finalMarking);
	}

	private Marking markPlaceFromCandidates(Set<Place> initialCandidates, Set<String> identifyingLabels) {
		Marking resultMarking = new Marking();
		for (String initLabel: identifyingLabels) {
			for (Place initPlace: initialCandidates) {
				if (initLabel.equals(initPlace.getLabel())){
					resultMarking.add(initPlace);
					break;
				}
			}
		}
		return resultMarking;
	}
	
	public AcceptingPetriNet createAcceptingNetArgs(String label, String ... specs) {
		if (specs.length == 0) {
			throw new RuntimeException("Cannot create empty Accepting Petri Net");
		}
		AcceptingPetriNet anet = createAcceptingNet(label,specs[0]);
		for (int i=1; i<specs.length; i++) {
			addToAcceptingNet(anet, specs[i]);
		}		
		return anet;
	}
	
	/**
	 * Precondition: the underlying <code>Petrinet</code> in <code>anet</code> is a 
	 * <code>StochasticNet</code>.
	 * 
	 * @param anet
	 * @param netText
	 */
	public void addToAcceptingNet(AcceptingPetriNet anet, String netText) {
		net = (StochasticNet)anet.getNet();
		addToNet(net,netText);
		anet = markInitialFinalPlaces(net);
	}

	public StochasticNet createNetArgs(String label, String ... specs) {
		if (specs.length == 0) {
			return new StochasticNetImpl(label);
		}
		StochasticNet net = createNet(label, specs[0]);
		for (int i=1; i<specs.length; i++) {
			addToNet(net, specs[i]);
		}
		return net;
	}


	private void tokenize(String str) {
		tokens.clear();
		String s = new String(str);
		while (!s.equals("")) {
			boolean match = false;
		    for (PetriNetFragmentParser.TokenInfo info : TokenInfo.LEX_VALUES) {
		        Matcher m = info.pattern.matcher(s);
		        if (m.find()) {
		        	match = true;
		        	String tok = m.group().trim();
		        	tokens.add(new Token(info, tok));
		        	s = m.replaceFirst("");
		        	break;
		        }
		    }
		    if (!match) 
		    	throw new RuntimeException("Unexpected character in input:"+s);
		}
    }

	private void parse() {
		lookahead = tokens.getFirst();
		petriOnelineNet();
		if (lookahead.tokenInfo != TokenInfo.TERMINAL)
			throw new RuntimeException("Unexpected symbol " + lookahead + " found");
	}


	private void petriOnelineNet() {
		Place p1 = place();
		edge();
		Transition transition = transition();
		edge();
		Place p2 = placeLedSubnet();
		net.addArc(p1, transition);
		net.addArc(transition, p2);			
	}
	
	private Place placeLedSubnet() {
		Place head = place();
		if (lookahead.tokenInfo == TokenInfo.EDGE) {
			edge();
			Transition transition = transition();
			edge();
			Place tail = placeLedSubnet();
			net.addArc(head, transition);
			net.addArc(transition, tail);
		}
		return head;
	}

	private Transition transition() {
		Transition transition = null;
		if (lookahead.tokenInfo.equals(TokenInfo.SIMPLE_TRANSITION)) {
			transition = simpleTransition();
		}
		if (lookahead.tokenInfo.equals(TokenInfo.WEIGHTED_VALUE_TRANSITION)) {
			transition = weightedValueTransition();
		}
		if (lookahead.tokenInfo.equals(TokenInfo.WEIGHTED_DEFAULT_TRANSITION)) {
			transition = weightedDefaultTransition();
		}		
		nextToken();
		return transition;
	}

	private Transition weightedDefaultTransition() {
		Transition transition;
		// This is cheating / a hack 
		// We are tokenizing inside the parser to keep the structure LL(1)
		String label = lookahead.sequence.substring(1,lookahead.sequence.length()-1);
		transition = checkExistingTransition(label);
		if (transition == null) {
			transition = net.addImmediateTransition(label, 1.0 );
			nodeLookup.put(label,transition);
		}
		return transition;
	}

	private Transition weightedValueTransition() {
		Transition transition;
		// This is cheating / a hack 
		// We are tokenizing inside the parser to keep the structure LL(1)
		String text = lookahead.sequence.substring(1,lookahead.sequence.length()-1);
		String[] values = text.split(" ");
		String label = values[0];
		transition = checkExistingTransition(label);
		if (transition == null) {
			transition = net.addImmediateTransition(values[0], Double.valueOf( values[1] ));
			nodeLookup.put(label,transition);
		}
		return transition;
	}

	private Transition simpleTransition() {
		Transition transition;
		String label = lookahead.sequence.substring(1,lookahead.sequence.length()-1);
		transition = checkExistingTransition(label);
		if (transition == null) {
			transition = net.addTransition(label);
			nodeLookup.put(label,transition);
		}
		return transition;
	}
	
	private Transition checkExistingTransition(String label) {
		return (Transition)checkExistingNode(label,Transition.class);
	}
	
	private Place checkExistingPlace(String label) {
		return (Place)checkExistingNode(label,Place.class);
	}

	
	private PetrinetNode checkExistingNode(String label, Class<?> expectedClass) {
		PetrinetNode existing = nodeLookup.get(label);
		if (existing != null)
			if (!(expectedClass.isInstance(existing))) {
				throw new RuntimeException("New node " + label + " duplicates existing node of wrong type");
			}
		return existing;
	}


	private void edge() {
		if (lookahead.tokenInfo != TokenInfo.EDGE)
			throw new RuntimeException("Expected ->, but found " + lookahead );
		nextToken();		
	}


	private Place place() {
		String label = lookahead.sequence;
		Place place = checkExistingPlace(label);
		if (place == null) {
			place = net.addPlace(label);
			nodeLookup.put(label,place);
		}
		nextToken();
		return place;
	}

	private void nextToken() {
		tokens.pop();
		// at the end of input we return an epsilon token
		if (tokens.isEmpty())
			lookahead = new Token(TokenInfo.TERMINAL, "");
		else
			lookahead = tokens.getFirst();
	}		
	
}