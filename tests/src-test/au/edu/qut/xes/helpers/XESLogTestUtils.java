package au.edu.qut.xes.helpers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

public class XESLogTestUtils {

	public static boolean logEventNamesEqual(XLog log1, XLog log2) {
		if (log1 == null && log2 == null) {
			return true;
		}
		if (log1 == null || log2 == null) {
			return false;
		}
		if (log1.size() != log2.size()) {
			return false;
		}
		for (int i=0; i<log1.size(); i++) {
			XTrace trace1 = log1.get(i);
			XTrace trace2 = log2.get(i);
			if (trace1.size() != trace2.size()) {
				return false;
			}
			for (int j=0; j<trace1.size(); j++) {
				 XEvent event1 = trace1.get(j);
				 XEvent event2 = trace2.get(j);
				 if (! event1.getAttributes().get("concept:name").equals( 
						  event2.getAttributes().get("concept:name") ) ) 
				 {
					 return false;
				 }
			}			
		}
		return true;
	}

	public static String formatLog(XLog log) {
		StringBuffer result = new StringBuffer();
		result.append("[");
		boolean first = true;
		for (XTrace trace: log) {
			if (first) {
				first = false;
			}else {
				result.append(",");
			}
			boolean firstEvent = true;
			for (XEvent event: trace) {
				if (firstEvent) {
					firstEvent = false;
				}else {
					result.append(",");
				}
				result.append(event.getAttributes());				
			}
		}
		result.append("]");
		return result.toString();
	}

	public static void compareLogs(XLog expectedLog, XLog result) {
		assertEquals (String.format("Log size expected {} != {}", expectedLog.size(), result.size()), 
				expectedLog.size(), result.size() );
		assertTrue( formatLog(expectedLog) + " != " + formatLog(result), 
				logEventNamesEqual(expectedLog,result) );
	}

}
