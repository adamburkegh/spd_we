package au.edu.qut.pm.util;

/**
 * This class mimics part of the interface for class 
 * <pre>org.apache.logging.log4j.LogManager</pre>. log4j dependencies are
 * manually stripped during the ProM build process since around ProM 6.13,
 * due to a build server cybersecurity incident that would never impact 
 * this code, and which does not impact later versions of log4j.
 * 
 * To workaround this build restriction, this class provides more basic 
 * log manager functionality.
 * 
 */
public class LogManager {

	// Yes, singletons are discouraged; no, here it doesn't matter.
	private static Logger INSTANCE = new Logger(Logger.Level.INFO);
	
	public static Logger getLogger() {
		return INSTANCE;
	}
	
}
