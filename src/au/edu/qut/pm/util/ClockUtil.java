package au.edu.qut.pm.util;

import java.time.ZonedDateTime;
import java.util.Date;

public class ClockUtil {

	public static interface Clock{
		public long currentTimeMillis();
		public Date currentTime();
		public String nowString();
	}

	private static class DefaultClock implements Clock{
		public long currentTimeMillis() {
			return System.currentTimeMillis();
		}

		public Date currentTime() {
			return new Date();
		}
		
		public String nowString() {
			return ZonedDateTime.now().toString();
		}
		
	}

	
	
	private volatile static Clock CLOCK = new DefaultClock();
	
	public static long currentTimeMillis() {
		return CLOCK.currentTimeMillis();
	}
	
	public static Date currentTime() {
		return CLOCK.currentTime();
	}
	
	public static String nowString() {
		return CLOCK.nowString();
	}
	
	/**
	 * Intended for test scaffolding.
	 * 
	 * @param clock
	 */
	public static void setOverrideClock(Clock clock) {
		CLOCK = clock;
	}
	
}
