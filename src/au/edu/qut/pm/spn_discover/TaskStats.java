package au.edu.qut.pm.spn_discover;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementMap;
import org.simpleframework.xml.Root;

import au.edu.qut.pm.util.ClockUtil;

@Root
public class TaskStats{

	private static String SEP = " -- ";
	
	@Attribute
	private String taskName = "";
	
	@Element
	private long duration = 0;
	
	@Element
	private long start = 0;
	
	@Element(required=false)
	private String errorMessage = "";
	
	@Attribute
	private RunState runState = RunState.INITIALIZING;
	
	@ElementMap(entry="measureStats", key="measure", attribute=true, inline=true, required=false)
	private Map<Measure,Number> measures = new HashMap<Measure,Number>();

	public TaskStats(String taskName) {
		this.taskName = taskName;
	}
	
	@SuppressWarnings("unused")
	private TaskStats() {
		
	}
	
	public String getTaskName() {
		return taskName;
	}
	public long getDuration() {
		return duration;
	}
	public boolean isSuccess() {
		return runState == RunState.SUCCESS;
	}
	public RunState getRunState() {
		return runState;
	}
	public void setMeasure(Measure measure, Number value) {
		this.measures.put(measure, value);
	}
	public Map<Measure,Number> getMeasures(){
		return Collections.unmodifiableMap(measures);
	}
	public void markEnd() {
		runState = RunState.SUCCESS;
		duration = ClockUtil.currentTimeMillis() - start;
	}
	public void markRunning() {
		runState = RunState.RUNNING;
		start = ClockUtil.currentTimeMillis();
	}
	public void markFailed(String errorMessage) {
		runState = RunState.FAILED;
		this.errorMessage = errorMessage; 
		duration = ClockUtil.currentTimeMillis() - start;
	}

	public String getErrorMessage() {
		return errorMessage;
	}	

	
	public String formatStats() {
		return "Run " + runState + SEP + 
				"Duration: " + getDuration() + " ms" + SEP +
				measures;
	}

			
}