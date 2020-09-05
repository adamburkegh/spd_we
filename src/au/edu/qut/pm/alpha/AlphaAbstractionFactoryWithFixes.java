package au.edu.qut.pm.alpha;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.alphaminer.abstractions.AlphaRobustAbstraction;
import org.processmining.alphaminer.abstractions.impl.AlphaRobustAbstractionImpl;
import org.processmining.alphaminer.parameters.AlphaRobustMinerParameters;
import org.processmining.logabstractions.factories.ActivityCountAbstractionFactory;
import org.processmining.logabstractions.factories.DirectlyFollowsAbstractionFactory;
import org.processmining.logabstractions.factories.LoopAbstractionFactory;
import org.processmining.logabstractions.factories.StartEndActivityFactory;
import org.processmining.logabstractions.util.XEventClassUtils;

/**
 * Re-implements methods from 
 * <code>org.processmining.alphaminer.abstractions.AlphaAbstractionFactory</code> to fix bugs found.
 * 
 * @author burkeat
 *
 */
public class AlphaAbstractionFactoryWithFixes {

	/**
	 * At time of writing, the original by Loek Tonnaer / Eric Verbeek has a bug where activity 
	 * count is missed for traces of length one.
	 * 
	 * @param log
	 * @param classifier
	 * @param parameters
	 * @return
	 */
	public static AlphaRobustAbstraction<XEventClass> createAlphaRobustAbstraction(XLog log,
			XEventClassifier classifier, AlphaRobustMinerParameters parameters) {
		XEventClasses classes = XEventClasses.deriveEventClasses(classifier, log);
		double[][] dfa = new double[classes.size()][classes.size()]; // directly follows (count)
		double[] starts = new double[classes.size()]; // start activity
		double[] ends = new double[classes.size()]; // end activity
		double[] lol = new double[classes.size()]; // length one loop
		double[] ac = new double[classes.size()]; // activity count
		for (XTrace trace : log) {
			if (!trace.isEmpty()) {
				starts[classes.getClassOf(trace.get(0))
						.getIndex()] += StartEndActivityFactory.DEFAULT_THRESHOLD_BOOLEAN;
				ends[classes.getClassOf(trace.get(trace.size() - 1))
						.getIndex()] += StartEndActivityFactory.DEFAULT_THRESHOLD_BOOLEAN;
				if (trace.size() == 1) {
					ac[classes.getClassOf(trace.get(0)).getIndex()] 
							+= DirectlyFollowsAbstractionFactory.DEFAULT_THRESHOLD_BOOLEAN;
				}
				for (int i = 0; i < trace.size() - 1; i++) {
					XEventClass from = classes.getClassOf(trace.get(i));
					XEventClass to = classes.getClassOf(trace.get(i + 1));
					dfa[from.getIndex()][to.getIndex()] += DirectlyFollowsAbstractionFactory.DEFAULT_THRESHOLD_BOOLEAN;
					if (from.equals(to)) {
						lol[from.getIndex()] = LoopAbstractionFactory.DEFAULT_THRESHOLD_BOOLEAN;
					}
					ac[from.getIndex()] += DirectlyFollowsAbstractionFactory.DEFAULT_THRESHOLD_BOOLEAN;
					if (i == trace.size() - 2) { // count final activity as well
						ac[to.getIndex()] += DirectlyFollowsAbstractionFactory.DEFAULT_THRESHOLD_BOOLEAN;
					}
				}
			}
		}
		XEventClass[] arr = XEventClassUtils.toArray(classes);
		return new AlphaRobustAbstractionImpl<>(arr,
				DirectlyFollowsAbstractionFactory.constructDirectlyFollowsAbstraction(arr, dfa,
						DirectlyFollowsAbstractionFactory.DEFAULT_THRESHOLD_BOOLEAN),
				StartEndActivityFactory.constructStartActivityAbstraction(arr, starts,
						StartEndActivityFactory.DEFAULT_THRESHOLD_BOOLEAN),
				StartEndActivityFactory.constructEndActivityAbstraction(arr, ends,
						StartEndActivityFactory.DEFAULT_THRESHOLD_BOOLEAN),
				LoopAbstractionFactory.constructLengthOneLoopAbstraction(arr, lol,
						LoopAbstractionFactory.DEFAULT_THRESHOLD_BOOLEAN),
				ActivityCountAbstractionFactory.constructActivityCountAbstraction(arr, ac,
						ActivityCountAbstractionFactory.DEFAULT_THRESHOLD_BOOLEAN),
				parameters.getCausalThreshold(),
				parameters.getNoiseThresholdLeastFreq(),
				parameters.getNoiseThresholdMostFreq());
	}

	
}
