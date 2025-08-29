package au.edu.qut.pm.util;

import java.io.PrintStream;

/**
 * This class mimics part of the interface for class 
 * <pre>org.apache.logging.log4j.LogManager</pre>. log4j dependencies are
 * manually stripped during the ProM build process since around ProM 6.13,
 * due to a build server cybersecurity incident that would never impact 
 * this code, and which does not impact later versions of log4j.
 * 
 * To workaround this build restriction, this class provides more basic 
 * log functionality.
 * 
 * Not particularly high performance. Just works.
 * 
 */
public class Logger {
	
	private static final String PARAM = "{}";
	
	public static enum LogLevel{
		DEBUG,INFO,WARN,ERROR;
	}
	
	protected LogLevel level;
	protected PrintStream stream;

	protected Logger(LogLevel level, PrintStream stream) {
		this.level = level;
		this.stream = stream;
	}
	
	protected Logger(LogLevel level) {
		this(level, System.out);
	}

	protected void log(LogLevel atLevel, String string, Object[] objects) {
		if (level.compareTo(atLevel) > 0) {
			return;
		}
		StringBuffer outStr = new StringBuffer();
		int fromIndex = 0;
		int paramIndex = 0;
		while (fromIndex >= 0 && paramIndex <= objects.length) {
			System.out.println(outStr);
			int thisIndex = string.indexOf(PARAM, fromIndex);
			if (thisIndex == -1) {
				outStr.append(string.substring(fromIndex, string.length()));
			}else {
				outStr.append(string.substring(fromIndex, thisIndex));
				outStr.append(objects[paramIndex]);
			}
			fromIndex = thisIndex+PARAM.length();
			paramIndex += 1;
		}
		stream.println(outStr.toString());				
	}
	
	public void debug(String string) {
		log(LogLevel.DEBUG, string, new Object[] {});
	}	
	
	public void debug(String string, Object[] objects) {
		log(LogLevel.DEBUG,string,objects);
	}

	public void debug(String string, Object o1) {
		debug(string, new Object[] {o1});
	}
	
	public void debug(String string, Object o1, Object o2) {
		debug(string, new Object[] {o1,o2});
	}

	public void info(String string) {
		log(LogLevel.INFO, string, new Object[] {});
	}	


}
