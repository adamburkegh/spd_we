package au.edu.qut.prom.helpers;

import org.processmining.contexts.uitopia.UIContext;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.connections.Connection;
import org.processmining.framework.connections.ConnectionManager;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.PluginExecutionResult;
import org.processmining.framework.plugin.ProMFuture;
import org.processmining.framework.plugin.Progress;
import org.processmining.framework.plugin.events.Logger;

/**
 * For running plugins in headless mode from test scaffold or command line, even if they claim to require UIPluginContext and therefore
 * be GUI only. Adapted from Andreas Rogge-Solti's <code>StochasticNetUtils.FakePluginContext</code> and Sander Leemans' 
 * <code>thesis.helperClasses.FakeContext</code>. Uses a single static main plugin context.
 * 
 * @author burkeat
 *
 */
public class HeadlessUIPluginContext extends UIPluginContext{
	
	private static UIPluginContext MAIN_PLUGIN_CONTEXT;
	
    static {
        UIContext MAIN_CONTEXT = new UIContext();
        MAIN_PLUGIN_CONTEXT = MAIN_CONTEXT.getMainPluginContext().createChildContext("HeadlessPluginContext");
    }
		
    private PluginContext context;
    
	public HeadlessUIPluginContext(PluginContext context, String label) {
		super(MAIN_PLUGIN_CONTEXT,label);
		this.context = context;
	}
	
    @Override
    public ConnectionManager getConnectionManager() {
        return this.context.getConnectionManager();
    }

    @Override
    public <T extends Connection> T addConnection(T c) {
        return this.context.addConnection(c);
    }

    @Override
    public void clear() {
        this.context.clear();
    }

    @Override
    public Progress getProgress() {
        return context.getProgress();
    }

    @Override
    public ProMFuture<?> getFutureResult(int i) {
        return context.getFutureResult(i);
    }

    @Override
    public void setFuture(PluginExecutionResult futureToBe) {
        context.setFuture(futureToBe);
    }

    @Override
    public PluginExecutionResult getResult() {
        return context.getResult();
    }

    @Override
    public void log(String message, Logger.MessageLevel level) {
        context.log(message, level);
    }

    @Override
    public void log(String message) {
        context.log(message);
    }

    @Override
    public void log(Throwable exception) {
        context.log(exception);
    }
	
}
