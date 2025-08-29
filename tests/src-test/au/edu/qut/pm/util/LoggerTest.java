package au.edu.qut.pm.util;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.junit.Before;
import org.junit.Test;

import au.edu.qut.pm.util.Logger.LogLevel;

public class LoggerTest {

    protected ByteArrayOutputStream baos;
    protected static final String UTF8 = StandardCharsets.UTF_8.name();
    protected PrintStream ps;

    
    @Before
    public void setUp() throws Exception{
    	baos = new ByteArrayOutputStream();
    	ps = new PrintStream(baos, true, UTF8);
    }
	
	@Test
	public void zeroParamsDebug() throws Exception{
	    Logger logger = new Logger(LogLevel.DEBUG,ps);
	    logger.debug("Test message");
	    String result = baos.toString(UTF8).strip();
		assertEquals("Test message",result);
	}

	@Test
	public void zeroParamsInfo() throws Exception{
	    Logger logger = new Logger(LogLevel.DEBUG,ps);
	    logger.info("Test message");
	    String result = baos.toString(UTF8).strip();
		assertEquals("Test message",result);
	}

	
	@Test
	public void oneParam() throws Exception{
	    Logger logger = new Logger(LogLevel.DEBUG,ps);
	    logger.debug("Test {} thingy", "message");
	    String result = baos.toString(UTF8).strip();
		assertEquals("Test message thingy",result);
	}
	
	@Test
	public void oneParamStart() throws Exception{
	    Logger logger = new Logger(LogLevel.DEBUG,ps);
	    logger.debug("{} thingy", "Test");
	    String result = baos.toString(UTF8).strip();
		assertEquals("Test thingy",result);
	}

	
	@Test
	public void twoParams() throws Exception{
	    Logger logger = new Logger(LogLevel.DEBUG,ps);
	    logger.debug("Test {} thingami{}", "message", "whatsit");
	    String result = baos.toString(UTF8).strip();
		assertEquals("Test message thingamiwhatsit",result);
	}
	
	@Test
	public void belowLevel() throws Exception{
	    Logger logger = new Logger(LogLevel.INFO,ps);
	    logger.debug("Won't show");
	    String result = baos.toString(UTF8).strip();
		assertEquals("",result);
	}


	
}
