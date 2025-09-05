package au.edu.qut.prom.visualisers;

import javax.swing.JComponent;

import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.petrinet.StochasticNet;

import au.edu.qut.prom.visualisers.dot.SLPNDotVisualiser;

public class SLPNVisualiserPlugins {
	
	private SLPNVisualiserPlugins() {};
	
	@Plugin(name = ""
			+ "(Prettier) Stochastic labelled Petri net (simple weights) "
			+ "visualisation",
			returnLabels = {"(Prettier) Dot visualization" },
			returnTypes = { JComponent.class }, 
			parameterLabels = { "stochastic labelled Petri net", "canceller" },
			userAccessible = true, level = PluginLevel.NightlyBuild
			)
	@Visualizer
	@UITopiaVariant(
		affiliation = "QUT", 
		author = "Adam Banham", 
		email = "adam_banham@hotmail.com")
	@PluginVariant(
			variantLabel = ""
					+ "(Prettier) Stochastic labelled Petri net "
					+ "visualisation",
			requiredParameterLabels = { 0, 1 })
	public static final JComponent visualise(final PluginContext context, 
			StochasticNet net, ProMCanceller canceller) {
	return SLPNDotVisualiser.visualise(net);
	}

}
