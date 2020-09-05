package au.edu.qut.xes.helpers;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.PrintWriter;

import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
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
import org.junit.Before;
import org.junit.Test;


public class DelimitedTraceToXESConverterTest {

	private DelimitedTraceToXESConverter converter = null;
	
	@Before
	public void setup() {
		converter = new DelimitedTraceToXESConverter();
	}
	
	@Test
	public void emptyTrace() {
		final XLog emptyLog = new XLogImpl(null);
		XLog result = converter.convertText("");
		XESLogTestUtils.compareLogs(emptyLog, result);
	}
	
	@Test
	public void singleEvent() {
		final XLog expectedLog = newLog();
		XTrace trace = newTrace();
		newEvent("a", trace);
		expectedLog.add(trace);
		XLog result = converter.convertText("a");
		XESLogTestUtils.compareLogs(expectedLog, result);
	}

	private XEvent newEvent(String label, XTrace trace) {
		XAttributeMap attrs = new XAttributeMapImpl();
		XAttribute attr = new XAttributeLiteralImpl("concept:name", label);
		attrs.put("concept:name", attr);
		XEvent event = new XEventImpl(attrs);
		trace.add( event );
		return event;
	}

	private XTrace newTrace() {
		XAttributeMap traceAttrs = new XAttributeMapImpl();
		XTrace trace = new XTraceImpl(traceAttrs);
		return trace;
	}

	private XLog newLog() {
		XAttributeMap logAttrs = new XAttributeMapImpl();
		final XLog resultLog = new XLogImpl(logAttrs);
		return resultLog;
	}
	
	@Test
	public void singleTrace() {
		final XLog expectedLog = newLog();
		XTrace trace = newTrace();
		newEvent("a", trace);
		newEvent("b", trace);
		newEvent("c", trace);
		expectedLog.add(trace);
		XLog result = converter.convertText("a b c");
		XESLogTestUtils.compareLogs(expectedLog, result);
	}

	@Test
	public void singleTraceEventRepeat() {
		final XLog expectedLog = newLog();
		XTrace trace = newTrace();
		newEvent("a", trace);
		newEvent("b", trace);
		newEvent("a", trace);
		newEvent("a", trace);
		expectedLog.add(trace);
		XLog result = converter.convertText("a b a a");
		XESLogTestUtils.compareLogs(expectedLog, result);
	}
	
	@Test
	public void multiLineNoDupes() {
		final XLog expectedLog = newLog();
		XTrace trace = newTrace();
		newEvent("a", trace);
		newEvent("b", trace);
		newEvent("a", trace);
		newEvent("a", trace);
		expectedLog.add(trace);
		trace = newTrace();
		newEvent("c", trace);
		newEvent("b", trace);
		newEvent("a", trace);
		expectedLog.add(trace);
		String traceText = "a b a a" + "\n" + "c b a";
		XLog result = converter.convertText(traceText);
		XESLogTestUtils.compareLogs(expectedLog, result);
	}

	@Test
	public void multiLineWithDupes() {
		final XLog expectedLog = newLog();
		XTrace trace = newTrace();
		newEvent("a", trace);
		newEvent("b", trace);
		newEvent("a", trace);
		newEvent("a", trace);
		expectedLog.add(trace);
		trace = newTrace();
		newEvent("c", trace);
		newEvent("b", trace);
		newEvent("a", trace);
		expectedLog.add(trace);
		trace = newTrace();
		newEvent("a", trace);
		newEvent("b", trace);
		newEvent("a", trace);
		newEvent("a", trace);
		expectedLog.add(trace);
		String traceText = "a b a a" + "\n" 
		 				 + "c b a" + "\n"
						 + "a b a a" + "\n";
		XLog result = converter.convertText(traceText);
		XESLogTestUtils.compareLogs(expectedLog, result);
	}

	@Test
	public void varyDelimiter() {
		final XLog expectedLog = newLog();
		XTrace trace = newTrace();
		newEvent("a", trace);
		newEvent("b", trace);
		newEvent("a", trace);
		newEvent("a", trace);
		expectedLog.add(trace);
		trace = newTrace();
		newEvent("c", trace);
		newEvent("b", trace);
		newEvent("a", trace);
		expectedLog.add(trace);
		trace = newTrace();
		newEvent("a", trace);
		newEvent("b", trace);
		newEvent("a", trace);
		newEvent("a", trace);
		expectedLog.add(trace);
		String traceText = "a,b,a,a==" 
		 				 + "c,b,a=="
						 + "a,b,a,a==";
		XLog result = converter.convertText(traceText, ",", "==");
		XESLogTestUtils.compareLogs(expectedLog, result);
	}
	
	@Test
	public void convertFile() throws Exception  {
		String traceText = "b a c\n" +
					    "a c d d d e\n" +
					    "b a c\n" +
					    "ba d f";
		File testFile = File.createTempFile("delimited_trace_test", "txt");
		testFile.deleteOnExit();
		PrintWriter out = new PrintWriter(testFile.getAbsolutePath());
		out.print(traceText);
		out.flush();
		out.close();
		XLog expected = converter.convertText(traceText);		
		XLog result = converter.convertFile(testFile.getAbsolutePath());
		XESLogTestUtils.compareLogs(expected,result);
	}
	
	@Test
	public void convertTextVarargs() {
		final XLog expected = newLog();
		XTrace trace = newTrace();
		newEvent("a", trace);
		newEvent("b", trace);
		expected.add(trace);
		trace = newTrace();
		newEvent("c", trace);
		newEvent("d", trace);
		expected.add(trace);
		XLog result = converter.convertTextArgs("a b","c d");
		XESLogTestUtils.compareLogs(expected,result);
	}

	@Test
	public void logInfo() {
		XLog result = converter.convertText("a");
		XLogInfo xLogInfo = XLogInfoFactory.createLogInfo(result, new XEventNameClassifier());
		assertEquals(1, xLogInfo.getNumberOfTraces());
		assertEquals(1, xLogInfo.getNumberOfEvents());
	}
	
}
