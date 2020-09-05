package au.edu.qut.pm.alpha;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.alphaminer.abstractions.AlphaRobustAbstraction;
import org.processmining.alphaminer.parameters.AlphaRobustMinerParameters;
import org.processmining.framework.plugin.PluginContext;

public class StochasticAlphaMinerFactory {

	public static StochasticAlphaMiner<XEventClass> createActivityPairRHSAM(XEventClassifier classifier, 
			XLog log, PluginContext context)
	{
		AlphaRobustMinerParameters parameters = new AlphaRobustMinerParameters(0.0,0.0,0.0);
		AlphaRobustAbstraction<XEventClass> abstraction = 
				AlphaAbstractionFactoryWithFixes.createAlphaRobustAbstraction(log,classifier, parameters);
		return new StochasticAlphaMinerActivityPairRHImpl<>(abstraction,context);
	}

	public static StochasticAlphaMiner<XEventClass> createActivityPairLHSAM(XEventClassifier classifier, 
			XLog log, PluginContext context)
	{
		AlphaRobustMinerParameters parameters = new AlphaRobustMinerParameters(0.0,0.0,0.0);
		AlphaRobustAbstraction<XEventClass> abstraction = 
				AlphaAbstractionFactoryWithFixes.createAlphaRobustAbstraction(log,classifier, parameters);
		return new StochasticAlphaMinerActivityPairLHImpl<>(abstraction,context);
	}
	
	public static StochasticAlphaMiner<XEventClass> createMeanScaledActivityPairSAM(
			XEventClassifier classifier,XLog log, PluginContext context)
	{
		AlphaRobustMinerParameters parameters = new AlphaRobustMinerParameters(0.0,0.0,0.0);
		AlphaRobustAbstraction<XEventClass> abstraction = 
				AlphaAbstractionFactoryWithFixes.createAlphaRobustAbstraction(log,classifier, parameters);
		return new StochasticAlphaMinerMeanScaledActivityPairImpl<>(abstraction,context);
	}
	
	public static StochasticAlphaMiner<XEventClass> createEdgeStructuredSAM(XEventClassifier classifier, 
			XLog log, PluginContext context)
	{
		AlphaRobustMinerParameters parameters = new AlphaRobustMinerParameters(0.0,0.0,0.0);
		AlphaRobustAbstraction<XEventClass> abstraction = 
				AlphaAbstractionFactoryWithFixes.createAlphaRobustAbstraction(log,classifier, parameters);
		return new StochasticAlphaMinerEdgeStructuredImpl<>(abstraction,context);
	}

	public static StochasticAlphaMiner<XEventClass> createBillClintonSAM(XEventClassifier classifier, 
			XLog log, PluginContext context)
	{
		AlphaRobustMinerParameters parameters = new AlphaRobustMinerParameters(0.0,0.0,0.0);
		AlphaRobustAbstraction<XEventClass> abstraction = 
				AlphaAbstractionFactoryWithFixes.createAlphaRobustAbstraction(log,classifier, parameters);
		return new StochasticAlphaMinerBillClintonImpl<>(abstraction,context);
	}
	
}
