package au.edu.qut.pm.util;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import au.edu.qut.pm.util.ClockUtil.Clock;
import au.edu.qut.pm.util.Logger.Level;


public class LoggerTest {

	private static final String STATIC_TIME = "2025-08-29 14:57:01.001 Australia/Brisbane";
	
	private static class TestClock implements Clock{		

		public long currentTimeMillis() {
			return System.currentTimeMillis();
		}

		public Date currentTime() {
			return new Date();
		}
		
		public String nowString() {
			return STATIC_TIME;
		}
		
	}

	
    protected ByteArrayOutputStream baos;
    protected static final String UTF8 = StandardCharsets.UTF_8.name();
    protected PrintStream ps;

    @BeforeClass
    public static void beforeClass(){
    	ClockUtil.setOverrideClock(new TestClock());
    }
    
    @Before
    public void setUp() throws Exception{
    	baos = new ByteArrayOutputStream();
    	ps = new PrintStream(baos, true, UTF8);
    }
	
	@Test
	public void zeroParamsDebug() throws Exception{
	    Logger logger = new Logger(Level.DEBUG,ps);
	    logger.debug("Test message");
	    String result = baos.toString(UTF8).strip();
		assertEquals("DEBUG " + STATIC_TIME + " Test message",result);
	}

	@Test
	public void zeroParamsInfo() throws Exception{
	    Logger logger = new Logger(Level.DEBUG,ps);
	    logger.info("Test message");
	    String result = baos.toString(UTF8).strip();
		assertEquals("INFO " + STATIC_TIME + " Test message",result);
	}

	
	@Test
	public void oneParam() throws Exception{
	    Logger logger = new Logger(Level.DEBUG,ps);
	    logger.debug("Test {} thingy", "message");
	    String result = baos.toString(UTF8).strip();
		assertEquals("DEBUG " + STATIC_TIME + " Test message thingy",
				result);
	}
	
	@Test
	public void oneParamStart() throws Exception{
	    Logger logger = new Logger(Level.DEBUG,ps);
	    logger.debug("{} thingy", "Test");
	    String result = baos.toString(UTF8).strip();
		assertEquals("DEBUG " + STATIC_TIME + " Test thingy",result);
	}

	
	@Test
	public void twoParams() throws Exception{
	    Logger logger = new Logger(Level.DEBUG,ps);
	    logger.debug("Test {} thingami{}", "message", "whatsit");
	    String result = baos.toString(UTF8).strip();
		assertEquals("DEBUG " + STATIC_TIME + " Test message thingamiwhatsit",
				result);
	}
	
	@Test
	public void belowLevel() throws Exception{
	    Logger logger = new Logger(Level.INFO,ps);
	    logger.debug("Won't show");
	    String result = baos.toString(UTF8).strip();
		assertEquals("",result);
	}


	
}
