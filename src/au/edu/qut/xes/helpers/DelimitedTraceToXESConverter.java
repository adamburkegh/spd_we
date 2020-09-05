package au.edu.qut.xes.helpers;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;
import org.deckfour.xes.model.impl.XAttributeMapImpl;
import org.deckfour.xes.model.impl.XEventImpl;
import org.deckfour.xes.model.impl.XLogImpl;
import org.deckfour.xes.model.impl.XTraceImpl;

/**
 * Helper methods for converting a simple delimited file to an XES log. Intended mainly for concise, 
 * expressive test data. Expected syntax for input files is described in documentation for 
 * {@link #convertText(String, String, String)}.
 * 
 * @author burkeat
 *
 */
public class DelimitedTraceToXESConverter {

	public static final String DEFAULT_EVENT_DELIMITER = " ";
	public static final String DEFAULT_TRACE_DELIMITER = "\n";
	
	private static final String XES_CONCEPT_NAME = "concept:name";
	
	public DelimitedTraceToXESConverter() {
	}

	/**
	 * Convert a space delimited event log with one trace on each line 
	 * using {@link #convertText(String, String, String)}
	 * 
	 * @param traces
	 * @return
	 */
	public XLog convertText(String traces) {
		return convertText(traces,DEFAULT_EVENT_DELIMITER,DEFAULT_TRACE_DELIMITER);
	}
	
	/**
	 * Convert a trace sequence with one trace in each vararg. Each trace is a 
	 * sequence of space-delimited events per {@link #convertText(String, String, String)}. 
	 * 
	 * @param traces
	 * @return
	 */	
	public XLog convertTextArgs(String ... traces) {
		XAttributeMap attrMap = new XAttributeMapImpl();
		XLog result = new XLogImpl(attrMap);
		for (String line: traces) {
			if ("".equals(line))
				break;
			XTrace trace = convertSingleTrace(DEFAULT_EVENT_DELIMITER, line);
			result.add(trace);
		}
		return result;
	}
	
	/**
	 * Convert a simple delimited file to an XES log. This is most useful for test data as it does
	 * not allow for the filtering of columns. It does allow multiple events per line, making
	 * for more concise and readable test data files. 
	 * 
	 * Files are of the form 
	 * 
	 * LOG   :: TRACE {TRACE_DELIMITER TRACE}
	 * TRACE :: EVENT {EVENT_DELIMITER EVENT}
	 * EVENT :: <string label> 
	 * 
	 * @param traces
	 * @param eventDelimiter
	 * @param traceDelimiter
	 * @return
	 */
	public XLog convertText(String traces, String eventDelimiter, String traceDelimiter) {
		XAttributeMap attrMap = new XAttributeMapImpl();
		XLog result = new XLogImpl(attrMap);
		for (String line: traces.split(traceDelimiter)) {
			if ("".equals(line))
				break;
			XTrace trace = convertSingleTrace(eventDelimiter, line);
			result.add(trace);
		}
		return result;
	}

	private XTrace convertSingleTrace(String eventDelimiter, String line) {
		XAttributeMap traceAttrMap = new XAttributeMapImpl();
		XTrace trace = new XTraceImpl(traceAttrMap);
		for (String eventLabel: line.split(eventDelimiter)) {
			if ("".equals(eventLabel))
				break;
			XAttributeMap eventAttrMap = new XAttributeMapImpl();
			XAttribute attr = new XAttributeLiteralImpl(XES_CONCEPT_NAME,eventLabel);
			eventAttrMap.put(XES_CONCEPT_NAME, attr);
			XEvent event = new XEventImpl(eventAttrMap);
			trace.add(event);
		}
		return trace;
	}
	
	/**
	 * 
	 * 
	 * @param filePath
	 * @return
	 */
	public XLog convertFile(String filePath)
			throws IOException
	{
		return convertFile(filePath,DEFAULT_EVENT_DELIMITER,DEFAULT_TRACE_DELIMITER);
	}
	
	/**
	 * Small files only. UTF-8 encoding assumed.
	 * 
	 * @param filePath
	 * @param eventDelimiter
	 * @param traceDelimiter
	 * @return
	 */
	public XLog convertFile(String filePath, String eventDelimiter, String traceDelimiter)
		throws IOException
	{
		byte[] encoded = Files.readAllBytes( Paths.get( filePath ));
		return convertText( new String(encoded,StandardCharsets.UTF_8) );
	}
	
	
	
}
