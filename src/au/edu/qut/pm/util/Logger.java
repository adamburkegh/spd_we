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
	
	public static enum Level{
		DEBUG,INFO,WARN,ERROR;
	}
	
	protected Level level;
	protected PrintStream stream;

	protected Logger(Level level, PrintStream stream) {
		this.level = level;
		this.stream = stream;
	}
	
	protected Logger(Level level) {
		this(level, System.out);
	}

	public void log(Level atLevel, String string) {
		log(atLevel, string, new Object[] {});
	}
	
	protected void log(Level atLevel, String string, Object[] objects) {
		if (level.compareTo(atLevel) > 0) {
			return;
		}
		StringBuffer outStr = new StringBuffer();
		outStr.append(atLevel);
		outStr.append(" ");
		outStr.append(ClockUtil.nowString() );
		outStr.append(" ");
		int fromIndex = 0;
		int paramIndex = 0;
		while (fromIndex >= 0 && paramIndex <= objects.length) {
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
		log(Level.DEBUG, string);
	}	

	public void debug(Object o1) {
		log(Level.DEBUG, o1.toString());
	}	
	
	public void debug(String string, Object[] objects) {
		log(Level.DEBUG,string,objects);
	}

	public void debug(String string, Object o1) {
		debug(string, new Object[] {o1});
	}
	
	public void debug(String string, Object o1, Object o2) {
		debug(string, new Object[] {o1,o2});
	}

	public void debug(String string, Object o1, Object o2, Object o3) {
		debug(string, new Object[] {o1,o2,o3});
	}

	public void debug(String string, Object o1, Object o2, Object o3, Object o4) {
		debug(string, new Object[] {o1,o2,o3,o4});
	}

	public void debug(String string, Object o1, Object o2, Object o3, Object o4, Object o5) {
		debug(string, new Object[] {o1,o2,o3,o4,o5});
	}
	
	public void info(String string) {
		log(Level.INFO, string);
	}	

	public void info(String string, Object[] objects) {
		log(Level.INFO,string,objects);
	}
	
	public void info(String string, Object o1) {
		info(string, new Object[] {o1});
	}

	public void info(String string, Object o1, Object o2) {
		info(string, new Object[] {o1,o2});
	}

	public void info(String string, Object o1, Object o2, Object o3) {
		info(string, new Object[] {o1,o2,o3});
	}
	
	public void error(String string, Object[] objects) {
		log(Level.ERROR,string,objects);
	}

	public void error(String string) {
		log(Level.ERROR,string);
	}
	
	public void error(String string, Object o1) {
		log(Level.ERROR,string, new Object[] {o1});
	}
	
	public void error(String string, Object o1, Object o2) {
		log(Level.ERROR,string, new Object[] {o1,o2});
	}

	public void error(String string, Object o1, Exception e) {
		log(Level.ERROR,string, new Object[] {o1});
		logException(e);
	}

	private void logException(Exception e) {
		stream.println(e);
	}


}
