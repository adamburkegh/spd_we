package au.edu.qut.pm.spn_discover;

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import au.edu.qut.pm.util.ClockUtil;

@Root
public class RunStats {

	private static final String LINE_SEP = "\n ==== ";
	private static final String SEP = " -- ";
	
	private static String BUILD_VERSION = "";
	static {
		// This is not really the best place - ModelRunner would be better
		// ... or the best way - reading a version off a jar file would be a better first choice
		// ... with this perhaps a fallback
		// But our jar file is unversioned for now, and this does the job.
		// buildid.txt is usually created by ant build.xml at build time and is helpful.
		try {
			byte[] encoded = Files.readAllBytes( Paths.get( "buildid.txt" ));
			String text = new String(encoded,StandardCharsets.UTF_8);
			BUILD_VERSION = text.split(" ")[1];
		}catch(Exception e) {
		}
	}

	
	@Element
	protected String inputLogFileName;
	
	@Element
	protected String outputModelFileName;
	
	@Attribute
	protected String miner;
	
	@Attribute
	protected String machineName;

	@Attribute(required=false)
	protected String runnerVersion = "";
	
	@Element 
	protected Date runDate;
	
	@ElementList
	protected List<TaskStats> taskRunStats = new LinkedList<TaskStats>();
	
	@Element(required=false)
	protected String errorMessage = "";

	private Map<Measure,Number> allMeasures = new TreeMap<Measure,Number>();
	private Map<Measure,String> measureSources = new HashMap<Measure,String>();
	
	@Element
	private RunState runState = RunState.INITIALIZING;
	
	public RunStats(String inputLogFileName, String outputModelFileName, String miner) 
	{
		this.inputLogFileName = inputLogFileName;
		this.outputModelFileName = outputModelFileName;
		this.miner = miner;
		this.runDate = ClockUtil.currentTime();
		try {
			this.machineName = InetAddress.getLocalHost().getHostName();
		}catch (Exception e){
			this.machineName = "Unkown (" + e.getMessage() + ")";
		}
		runnerVersion = BUILD_VERSION;
	}
	
	protected RunStats(RunStats other) {
		this.inputLogFileName = other.inputLogFileName;
		this.outputModelFileName = other.outputModelFileName;
		this.miner = other.miner;
		this.runDate = other.runDate;
		this.runState = other.runState;
		this.machineName = other.machineName;
		this.taskRunStats = other.taskRunStats;
		this.errorMessage = other.errorMessage;
		this.runnerVersion = other.runnerVersion;
	}
	
	@SuppressWarnings("unused")
	private RunStats() {
		
	}

	public RunState getState() {
		return runState;
	}
	
	public void addTask(TaskStats taskStats) {
		if (RunState.FAILED == taskStats.getRunState()) {
			markFailed(taskStats.getErrorMessage() );
		}else {
			runState = RunState.RUNNING;
		}
		taskRunStats.add(taskStats);
		acculumateMeasure(taskStats);
	}

	private void acculumateMeasure(TaskStats taskStats) {
		Map<Measure,Number> taskMeasures = taskStats.getMeasures(); 
		allMeasures.putAll(taskMeasures);
		for (Measure m: taskMeasures.keySet()) {
			measureSources.put(m,taskStats.getTaskName() );
		}
	}
	
	public void markEnd() {
		if (runState != RunState.FAILED)
			runState = RunState.SUCCESS;
	}
	
	public void markFailed(String errorMessage) {
		runState = RunState.FAILED;
		this.errorMessage = errorMessage;
	}
	
	public Map<Measure,Number> getAllMeasures(){
		if (allMeasures.isEmpty()) {
			for (TaskStats taskStats: taskRunStats) {
				acculumateMeasure(taskStats);				
			}
		}
		return allMeasures;
	}
	
	public String getArtifactCreator() {
		return miner;
	}
	
	public String formatStats() {
		StringBuilder result = new StringBuilder("Run " + runState + LINE_SEP);
		for (TaskStats task : taskRunStats) {
			result.append(task.getTaskName());
			result.append(SEP);
			result.append(task.getRunState());
			result.append(SEP);
			result.append(task.getDuration());
			result.append(" ms");
			result.append(LINE_SEP);
		}
		result.append(allMeasures);
		return result.toString();
	}	
		
	
}
