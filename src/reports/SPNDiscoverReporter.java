package reports;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import au.edu.qut.pm.util.LogManager;
import au.edu.qut.pm.util.Logger;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import au.edu.qut.pm.spn_discover.ClassicAlphaMiner;
import au.edu.qut.pm.spn_discover.ControlInductiveMiner;
import au.edu.qut.pm.spn_discover.FlowerMiner;
import au.edu.qut.pm.spn_discover.Measure;
import au.edu.qut.pm.spn_discover.RunState;
import au.edu.qut.pm.spn_discover.RunStats;
import au.edu.qut.pm.spn_discover.StochasticNetLogMiner;
import au.edu.qut.pm.spn_discover.TaskStats;

public class SPNDiscoverReporter {

	private static final String ROGGE_SOLTI = "rssmt";

	
	protected static class ReportStats extends RunStats implements Comparable<ReportStats>{
		protected static class ReportStatsComparator implements Comparator<ReportStats>{

			@Override
			public int compare(ReportStats o1, ReportStats other) {
				int result = o1.inputLogFileName.compareTo(other.inputLogFileName);
				if (result != 0)
					return result;
				result = o1.minerAlgo.compareTo(other.minerAlgo);
				if (result != 0)
					return result;
				result = o1.artifactCreatorId.compareTo(other.artifactCreatorId);
				if (result != 0)
					return result;
				result = o1.miner.compareTo(other.miner);
				return result;
			}
			
		}
		
		private static final String MINER_TASK = "miner";
		private static final String ESTIMATE_TASK = "spmrunner_estimator";
		public String artifactCreatorId;;
		public String minerAlgo;
		public String resultFile;
		public long minerDuration = 0;
		public long estimatorDuration = 0;
		
		public ReportStats(RunStats runStats,
						   String artifactCreatorId, String minerAlgo, String resultFile) 
		{
			super(runStats);
			this.artifactCreatorId = artifactCreatorId;
			this.minerAlgo = minerAlgo;
			this.resultFile = resultFile;
			initDurations();
		}

		private void initDurations() {
			for (TaskStats task: taskRunStats) {
				if (RunState.SUCCESS != task.getRunState())
					continue;
				if (MINER_TASK.equals(task.getTaskName())) {
					minerDuration = task.getDuration();
				}
				if (ESTIMATE_TASK.equals(task.getTaskName())) {
					estimatorDuration = task.getDuration();
				}
			}			
		}

		@Override
		public int compareTo(ReportStats other) {
			int result = inputLogFileName.compareTo(other.inputLogFileName);
			if (result != 0)
				return result;
			result = minerAlgo.compareTo(other.minerAlgo);
			if (result != 0)
				return result;
			result = artifactCreatorId.compareTo(other.artifactCreatorId);
			if (result != 0)
				return result;
			result = this.miner.compareTo(other.miner);
			return result;
		}
		
		public String getInputLogFileName() {
			return inputLogFileName;
		}

		public String getMachineName() {
			return machineName;
		}

		public void mergeFrom(ReportStats other) {
			Set<String> taskKeys = new HashSet<>();
			for (TaskStats task: taskRunStats) {
				taskKeys.add(task.getTaskName());
			}
			for (TaskStats task: other.taskRunStats) {
				if (!taskKeys.contains(task.getTaskName())) {
					taskRunStats.add(task);
				}
			}
			includeMinerDuration(other);
			if (estimatorDuration == 0) {
				estimatorDuration = other.estimatorDuration;
			}
		}

		private void includeMinerDuration(ReportStats other) {
			if (!isMinerIncluded()) {
				minerDuration = other.minerDuration;
			}
		}

		public boolean isMinerIncluded() {
			return minerDuration != 0;
		}
		
	}
	
	private static final String RESULT_FILE_PREFIX = "mrun_";
	public static final String LINE_SEP = "\n";
	private static final Set<Measure> INCLUDED_MEASURES; 
	protected static Map<String,StochasticNetLogMiner> MINER_MAP = new HashMap<>();

	private static Logger LOGGER = LogManager.getLogger();
	
	static {
		INCLUDED_MEASURES = new TreeSet<>();
		registerMeasures();
		registerMiners();
	}

	private static void registerMeasures() {
		INCLUDED_MEASURES.add(Measure.MODEL_EDGE_COUNT);
		INCLUDED_MEASURES.add(Measure.MODEL_ENTITY_COUNT);
		INCLUDED_MEASURES.add(Measure.ENTROPY_PRECISION);
		INCLUDED_MEASURES.add(Measure.ENTROPY_RECALL);
		INCLUDED_MEASURES.add(Measure.EARTH_MOVERS_LIGHT_COVERAGE);
		INCLUDED_MEASURES.add(Measure.LOG_EVENT_COUNT);
		INCLUDED_MEASURES.add(Measure.LOG_TRACE_COUNT);
	}
	
	private static void registerMiners() {
		Set<StochasticNetLogMiner> miners = new HashSet<>();
		miners.add(new ClassicAlphaMiner());
		miners.add(new ControlInductiveMiner());
		miners.add(new FlowerMiner());
		for (StochasticNetLogMiner miner: miners) {
			MINER_MAP.put(miner.getShortID(), miner);
		}
	}
	
	protected String reportDir;
	protected Map<ReportStats,ReportStats> statsDb = new TreeMap<>(); // Set interface workaround
	protected ReportFormatter reportFormatter;
	protected PrintStream printStream;

	public SPNDiscoverReporter(String reportDir, ReportFormatter formatter, PrintStream outputStream) {
		this.reportDir = reportDir;
		reportFormatter = formatter;
		this.printStream = outputStream;
	}
	
	public SPNDiscoverReporter(String reportDir, String outFile)
		throws Exception
	{
		this(reportDir,new ReportFormatter(), new PrintStream( new FileOutputStream(outFile)) );
	}
	
	public SPNDiscoverReporter(String reportDir) {
		this(reportDir,new ReportFormatter(), System.out);
	}


	public void report() throws Exception{
		LOGGER.info("Initializing ...");
		registerMiners();
		LOGGER.info("Loading report data from {}", reportDir);
		loadReportFilesInDir(reportDir);
		File[] reportSubDirs = new File(reportDir).listFiles(File::isDirectory);
		for (File reportSubDir: reportSubDirs) {
			loadReportFilesInDir(reportSubDir.getPath());
		}
		outputReports();
	}

	private void loadReportFilesInDir(String reportingDir) {
		String[] reportFiles = new File(reportingDir).list((dir1, name) -> name.contains(RESULT_FILE_PREFIX));
		loadReportFiles(reportingDir, reportFiles);
	}

	private void loadReportFiles(String reportingDir, String[] reportFiles) {
		for (String reportFile: reportFiles) {
			try {
				LOGGER.info("report -- {} -- {}", reportingDir, reportFile);		
				loadResult(reportingDir, reportFile);
			}catch (Exception e) {
				LOGGER.error(reportFile,e);
			}
		}
	}
	

	protected void outputReports() {
		reportFormatter.startReport();
		String[] headers = new String[INCLUDED_MEASURES.size() + 8];
		headers[0] = "Artifact Creator"; headers[1] = "Short Id"; headers[2] = "Miner algo";
		headers[3] = "Log"; headers[4] = "Miner Duration"; headers[5] = "Estimator Duration";
		int i=6;
		for (Measure measure: INCLUDED_MEASURES) {
			headers[i] = measure.toString();
			i++;
		}
		headers[i++]= "Run file";
		headers[i++]= "Machine";
		reportFormatter.reportHeader(headers);
		reportFormatter.reportNewline();
		for ( ReportStats reportStats : statsDb.keySet() ) {
			if (reportStats.minerAlgo.equals(reportStats.artifactCreatorId) ) {					
				StochasticNetLogMiner minerAlgo = MINER_MAP.get( reportStats.minerAlgo );
				if (minerAlgo != null && !minerAlgo.isStochasticNetProducer()) {
					continue;
				}
			}
			reportFormatter.reportStartLine();
			reportFormatter.reportCells(
					rewriteArtifactCreator( reportStats.getArtifactCreator() ), 
					reportStats.artifactCreatorId, reportStats.minerAlgo, 
					rewriteLogName(reportStats.getInputLogFileName()));
			reportFormatter.reportCells(reportStats.minerDuration, 
					reportStats.estimatorDuration);
			Set<Measure> runMeasures = reportStats.getAllMeasures().keySet();
			for (Measure measure: INCLUDED_MEASURES) {
				if (runMeasures.contains(measure)) {
					reportFormatter.reportCells(reportStats.getAllMeasures().get(measure).toString());
				}else {
					RunState state = reportStats.getState();
					if ( RunState.FAILED.equals( state ) || RunState.RUNNING.equals( state )) {
						reportFormatter.reportCells(state.toString());
					}else {
						reportFormatter.reportCells("    ");
					}
				}
			}
			reportFormatter.reportCells(reportStats.resultFile);
			reportFormatter.reportCells(reportStats.getMachineName() );
			reportFormatter.reportNewline();
		}
		reportFormatter.endReport();
		printStream.println(reportFormatter.format());
	}

	private void loadResult(String reportingDir, String resultFile) throws Exception{
		Serializer serializer = new Persister();
		RunStats runStats = serializer.read(RunStats.class, new File(reportingDir + File.separator + resultFile));
		String clipped = resultFile.substring(RESULT_FILE_PREFIX.length());
		String[] ids = clipped.split("_");
		String artifactCreatorId = ids[0];
		String[] me = artifactCreatorId.split("-");
		String minerAlgo = artifactCreatorId;
		if (me.length == 2) {
			minerAlgo = me[1];
		}
		String log = ids[1];
		if (ids.length == 3) {
			log = log + "_" + ids[2];
		}
		ReportStats reportStats = new ReportStats(runStats,artifactCreatorId,minerAlgo,resultFile);
		ReportStats existing = checkAndMerge(reportingDir, resultFile, reportStats);
		if (minerAlgo.contentEquals( artifactCreatorId) // miner  
				&& !minerAlgo.equals(ROGGE_SOLTI) 		// direct discovery
				&& existing.isMinerIncluded() )	  
		{ 
			updateMinerDetails(minerAlgo, existing);
		}else {
			statsDb.put(existing,existing);
		}
	}

	private void updateMinerDetails(String minerAlgo, ReportStats existing) {
		for (ReportStats rs: statsDb.keySet() ) {
			if (!rs.isMinerIncluded() 
					&& minerAlgo.equals(rs.minerAlgo) 
					&& ! minerAlgo.equals(rs.artifactCreatorId ) ) 
			{
				rs.includeMinerDuration(existing);
			}
		}
	}

	private ReportStats checkAndMerge(String reportingDir, String resultFile, ReportStats reportStats) {
		ReportStats existing = statsDb.get(reportStats);
		if (existing == null) {
			existing = reportStats;
		}else {
			LOGGER.info("Merging from {} into {}", reportingDir, resultFile);
			existing.mergeFrom(reportStats);
		}
		return existing;
	}


	protected String rewriteArtifactCreator(String artifactCreator) {
		// Ugly, but the long text is in all the result files already
		if (artifactCreator.equals("Rogge-Solti StochasticMinerPlugin (Tracing)")) {
			return "Rogge-Solti Discovery";
		}
		if (artifactCreator.equals("Fork Distributed (Bill Clinton) Estimator")) {
			return "Fork Distributed Estimator";
		}
		return artifactCreator;
	}

	protected String rewriteLogName(String logFile) {
		if ("sepsis.xes".equals(logFile)) {
			return "Sepsis";
		}
		return logFile.substring(0, logFile.length()-4).replace("_", " ");
	}

	public static void main(String[] args) throws Exception{
		if (args.length == 2) {
			if ("paper".equals(args[1])){
				new SPNDiscoveryPaperReporter(args[0]).report();
			}else if ("psv".equals(args[1])){
				File f = new File(args[0]);
				String outFile = "var" + File.separator + f.getName() + ".psv";
				LOGGER.info("Outputting to file {}", outFile);
				new SPNDiscoverReporter(args[0], 
						outFile).report();
			}else {
				LOGGER.error("Invalid formatter {}",args[0]);
				System.exit(1);
			}
		}else {
			new SPNDiscoverReporter(args[0]).report();
		}
	}
}
