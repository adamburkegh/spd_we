package au.edu.qut.prom.helpers;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.StochasticNet;
import org.processmining.models.graphbased.directed.petrinet.StochasticNet.DistributionType;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.TimedTransition;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.stochasticpetrinet.StochasticNetUtils;

/**
 * Utility methods for exporting petri nets in visualization formats. 
 * 
 * Originally adapted from 
 * <code>org.processmining.models.graphbased.directed.petrinet.impl.ToStochasticNet</code>.
 * 
 * @author burkeat
 *
 */
public class PetrinetExportUtils {

    private static final double EPSILON = 0.0001;
	private static final String LINE_SEP = "\n";

	private static int checkId(PetrinetNode node, Map<PetrinetNode, String> idMapping, int currentCounter) {
        if (!idMapping.containsKey(node)) {
            idMapping.put(node, String.valueOf("id" + (currentCounter++)));
        }
        return currentCounter;
    }

	
    /**
     * Originally adopted from exportPN2DOT method from the EventToActivityMatcher plugin
     *
     * @param net
     * @author Thomas Baier, Andreas Rogge-Solti
     */
    public static String convertPetrinetToDOT(Petrinet net) {
        String lsep = System.getProperty("line.separator");

        String resultString = "digraph G { " + lsep;
        resultString += "ranksep=\".3\"; fontsize=\"14\"; remincross=true; margin=\"0.0,0.0\"; fontname=\"Arial\";rankdir=\"LR\";" + lsep;
        resultString += "edge [arrowsize=\"0.5\"];\n";
        resultString += "node [height=\".2\",width=\".2\",fontname=\"Arial\",fontsize=\"14\"];\n";
        resultString += "ratio=0.4;" + lsep;

        Map<PetrinetNode, String> idMapping = new HashMap<>();
        int id = 1;
        for (Transition tr : net.getTransitions()) {
            String label = tr.getLabel();
            String shape = "shape=\"box\"";
            if (tr instanceof TimedTransition) {
                TimedTransition tt = (TimedTransition) tr;
                label += "\\n" + StochasticNetUtils.printDistribution(tt.getDistribution());
                if (tt.getDistributionType().equals(DistributionType.IMMEDIATE)) {
                    shape += ",margin=\"0, 0.1\"";
                }
                double weight = tt.getWeight(); 
                if (weight > 0.0d ) {
                	if ( Math.abs( Math.round(weight) - weight ) < EPSILON ){
                		label += "\\n" + String.format("%d", Math.round(weight));
                	}else {
                		label += "\\n" + String.format("%.3f", weight);
                	}
                }
            }
            if (tr.isInvisible()) {
                shape += ",color=\"black\",fontcolor=\"white\"";
            }
            id = checkId(tr, idMapping, id);
            resultString += idMapping.get(tr) + " [" + shape + ",label=\"" + label + "\",style=\"filled\"];" + lsep;
        }


        // Places
        for (Place place : net.getPlaces()) {
            id = checkId(place, idMapping, id);
            resultString += idMapping.get(place) + " [shape=\"circle\",label=\"\"];" + lsep;
        }

        // Edges
        for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : net.getEdges()) {
            id = checkId(edge.getSource(), idMapping, id);
            id = checkId(edge.getTarget(), idMapping, id);

            String edgeString = idMapping.get(edge.getSource()) + " -> " + idMapping.get(edge.getTarget());
            resultString += edgeString + lsep;
        }

        resultString += "}";

        return resultString;
    }

    /**
     * Small files only - reads in one hit
     * 
     * @param filePath
     * @return
     * @throws IOException
     */
    public static String petriNetFragmentToDOT(String filePath) throws IOException 
    {
		byte[] encoded = Files.readAllBytes( Paths.get( filePath ));
		String text = new String(encoded,StandardCharsets.UTF_8); 
		PetriNetFragmentParser parser = new PetriNetFragmentParser();
		String[] lines = text.split(LINE_SEP);
		StochasticNet net = parser.createNetArgs(filePath, lines);
		return convertPetrinetToDOT(net);
    }
    
}
