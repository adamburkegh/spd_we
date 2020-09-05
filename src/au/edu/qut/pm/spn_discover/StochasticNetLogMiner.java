package au.edu.qut.pm.spn_discover;

import java.io.File;

import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;

public interface StochasticNetLogMiner extends StochasticArtifactRun{
	/**
	 * A <code>UIPluginContext</code> is passed in by <code>ModelRunner</code>, but it is up to the implementing class
	 * to downcast. This is almost always due to the underlying plugin relying on UIPluginContext
	 * instead of PluginContext, which is rightly discouraged.
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
	public default boolean isFilesytemLogReader() {
		return false;
	}
	public default void setLogFile(String logFile) {}
	
}