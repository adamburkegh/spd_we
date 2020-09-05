package au.edu.qut.pm.convert;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.petrinet.StochasticNet;
import org.processmining.plugins.pnml.importing.StochasticNetDeserializer;
import org.processmining.plugins.pnml.simple.PNMLRoot;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import au.edu.qut.prom.helpers.ConsoleUIPluginContext;
import au.edu.qut.prom.helpers.HeadlessDefinitelyNotUIPluginContext;
import au.edu.qut.prom.helpers.PetrinetExportUtils;

public class PetriNetConverter {
	
	private static boolean verbose = true;
	
	private static void log(String m) {
		if (verbose)
			System.out.println(m);
	}

	public static void main(String[] args) throws Exception{
		for (String fileIn: args) {
			processFile(fileIn);	
		}		
	}

	private static void processFile(String petriNetFileIn) throws Exception {
		File pnf = new File(petriNetFileIn);
		if (!pnf.exists()) {
			System.err.println("File " + petriNetFileIn + " not found");
			System.exit(1);
		}
		if (pnf.isDirectory()) {
			convertDirectory(pnf);
		}else {
			convertFile(pnf);
		}
	}

	private static void convertDirectory(File pnfDir) throws Exception{
		log("Converting directory " + pnfDir.getName());
		for (File pnf : pnfDir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.contains(".xml") || name.contains(".pnml");
			}
			
		}) ) {
			try {
				convertFile(pnf);
			}catch (Exception e) {
				System.err.println("Invalid file " + pnf.getName() + " " + e.getMessage() );
			}
		}		
	}

	private static void convertFile(File petriNetFileIn) throws Exception {
		String pnfName = petriNetFileIn.getName();
		log("Converting file " + pnfName);
		String prefix = pnfName.substring(0, pnfName.lastIndexOf(".") );
		StochasticNetDeserializer snd = new StochasticNetDeserializer();
		Serializer serializer = new Persister();
		PNMLRoot pnml = serializer.read(PNMLRoot.class, petriNetFileIn);
		PluginContext uipc = 
				new HeadlessDefinitelyNotUIPluginContext(new ConsoleUIPluginContext(), "spn_dot_converter");	
		Object[] obj = snd.convertToNet(uipc, pnml, pnfName, true);
		StochasticNet net = (StochasticNet)obj[0];
		String dot = PetrinetExportUtils.convertPetrinetToDOT(net);
		File outputFile = 
				new File(petriNetFileIn.getParentFile().getAbsolutePath() 
						+ File.separator + prefix + ".dot");
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
        writer.write(dot);
        writer.flush();
        writer.close();
        log("Output written to " + outputFile.getAbsolutePath());
	}
	
	
	
}
