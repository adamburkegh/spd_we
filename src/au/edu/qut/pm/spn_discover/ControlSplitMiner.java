package au.edu.qut.pm.spn_discover;

import java.io.File;
import java.io.IOException;

import au.edu.qut.pm.util.LogManager;
import au.edu.qut.pm.util.Logger;
import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.petrinet.StochasticNet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.pnml.importing.StochasticNetDeserializer;
import org.processmining.plugins.pnml.simple.PNMLRoot;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import au.edu.qut.pm.stochastic.StochasticNetDescriptor;
import au.edu.qut.prom.helpers.ConsoleUIPluginContext;
import au.edu.qut.prom.helpers.HeadlessDefinitelyNotUIPluginContext;
import au.edu.qut.prom.helpers.StochasticPetriNetUtils;

public class ControlSplitMiner implements StochasticNetLogMiner {

	
	private static final Logger LOGGER = LogManager.getLogger();
	
	private StochasticNetDescriptor result = null;
	private String logFile;
	
	@Override
	public String getShortID() {
		return "split";
	}
	
	@Override
	public String getReadableID() {
		return "Split Miner";
	}

	@Override
	public void run(PluginContext uipc, XLog log, File outputModelFile) throws Exception {
		File logFileObj = new File(logFile);
		LOGGER.info("Invoking split miner out of process on {}", logFile);
		callSplitMiner( logFileObj, outputModelFile );
		Serializer serializer = new Persister();
		PNMLRoot pnml = serializer.read(PNMLRoot.class, outputModelFile);
		StochasticNetDeserializer converter = new StochasticNetDeserializer();
		PluginContext pc = 
				new HeadlessDefinitelyNotUIPluginContext(new ConsoleUIPluginContext(), 
						"controlsplitminer_loadnet");
		Object[] cResult = converter.convertToNet(pc, pnml, outputModelFile.getName(), true);
		StochasticNet net = (StochasticNet)cResult[0];
		Marking initialMarking = (Marking)cResult[1];
		Marking sInitMarking = 
				StochasticPetriNetUtils.findEquivalentInitialMarking(initialMarking, net);
		result = new StochasticNetDescriptor(net.getLabel(), net, sInitMarking );
	}

	/**
	 * From Leemans caise2020multilevel.AlgorithmSplitMiner, and yes you are right, this is ugly.
	 * But that's the way splitminer is made available. 
	 * 
	 * @param logFile
	 * @param modelFile
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public static void callSplitMiner(File logFile, File modelFile) throws InterruptedException, IOException {
		//Split miner automatically puts .pnml after the file name
		File out = new File(modelFile.getParentFile().getAbsolutePath(),
				modelFile.getName().substring(0, modelFile.getName().length() - 5));
		String libpath = new File("lib" + File.separator + "splitminer").getAbsolutePath() + File.separator;
		String devpath = new File("ldlib" + File.separator + "splitminer").getAbsolutePath() + File.separator;
		String classpath = libpath + "splitminer.jar" + File.pathSeparator 
				+ libpath + "lib" + File.separator + "*" + File.pathSeparator
				+ devpath + "splitminer.jar" + File.pathSeparator
				+ devpath + "lib" + File.separator + "*";
		LOGGER.debug("Command classpath: {}", classpath);
		ProcessBuilder pb = new ProcessBuilder("java", "-cp", classpath,
				"au.edu.unimelb.services.ServiceProvider", "SMPN", "0.1", "0.4", "false", logFile.getAbsolutePath(),
				out.getAbsolutePath());
		String workingDir = modelFile.getAbsoluteFile().getParent() ;
		LOGGER.info("Split miner working directory {} ", workingDir);
		pb.directory( new File(workingDir) );
		pb.inheritIO().start().waitFor();
	}



	@Override
	public StochasticNetDescriptor getStochasticNetDescriptor() {
		return result;
	}
	
	@Override
	public boolean isStochasticNetProducer() {
		return false;
	}

	@Override
	public boolean isFilesytemLogReader() {
		return true;
	}
		
	public void setLogFile(String logFile) {
		this.logFile = logFile;
	}
}
