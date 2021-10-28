package au.edu.qut.pm.spn_discover;

import java.io.File;

import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;

import au.edu.qut.pm.stochastic.ProcessTreeConverter;
import au.edu.qut.pm.stochastic.StochasticNetDescriptor;

public interface StochasticNetLogMiner extends StochasticArtifactRun{
	/**
	 * A <code>UIPluginContext</code> is passed in by <code>ModelRunner</code>, but it is up to the 
	 * implementing classto downcast. This is almost always due to the underlying plugin relying on 
	 * UIPluginContext instead of PluginContext, which is rightly discouraged.
	 * 
	 * @param uipc
	 * @param log
	 * @param outputModelFile
	 * @throws Exception
	 */
	public void run(PluginContext uipc, XLog log, File outputModelFile) throws Exception;
	public StochasticNetDescriptor getStochasticNetDescriptor();
	public default boolean isStochasticNetProducer() {
		return true;
	}
	// Could move these two down to a subclass
	public default ProcessTreeConverter getConverter() {
		return null;
	}
	public default boolean isProcessTreeProducer() {
		return false;
	}
	public default boolean isFilesytemLogReader() {
		return false;
	}
	public default void setLogFile(String logFile) {}
	
}