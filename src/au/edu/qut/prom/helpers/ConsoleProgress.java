package au.edu.qut.prom.helpers;

import au.edu.qut.pm.util.LogManager;
import au.edu.qut.pm.util.Logger;
import org.processmining.framework.plugin.Progress;

public class ConsoleProgress implements Progress {
	
	private static Logger LOGGER = LogManager.getLogger();
	
	private int max = 100;
	private int current = 0;
	private boolean show = true;
	private String message = "-> ";
	private int progressIndicatorSample = 500;

	public void setValue(int value) {
	    current = value;
	    show();
	}

	public void setMinimum(int value) {
	}

	public void setMaximum(int value) {
	    max = value;
	}

	public void setIndeterminate(boolean makeIndeterminate) {
	     show = !makeIndeterminate;
	}

	public void setCaption(String message) {
	    this.message = message;
	}

	public boolean isIndeterminate() {
	    return show;
	}

	public boolean isCancelled() {
	    return false;
	}

	public void inc() {
	    current++;
	    show();
	}

	public int getValue() {
	    return current;
	}

	public int getMinimum() {
	    return 0;
	}

	public int getMaximum() {
	    return max;
	}

	public String getCaption() {
	    return message;
	}

	public void cancel() {
	}

	private void show() {
		if (show && (current % progressIndicatorSample == 0 )) {
	    	LOGGER.debug(message + " -> (" + current + " / " + max + " )" );
	    }
	}
}