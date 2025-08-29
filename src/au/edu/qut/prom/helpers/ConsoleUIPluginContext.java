package au.edu.qut.prom.helpers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

import au.edu.qut.pm.util.Logger.Level;
import au.edu.qut.pm.util.LogManager;
import au.edu.qut.pm.util.Logger;
import org.deckfour.xes.model.XLog;
import org.processmining.framework.connections.Connection;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.connections.ConnectionManager;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.PluginContextID;
import org.processmining.framework.plugin.PluginDescriptor;
import org.processmining.framework.plugin.PluginExecutionResult;
import org.processmining.framework.plugin.PluginManager;
import org.processmining.framework.plugin.PluginParameterBinding;
import org.processmining.framework.plugin.ProMFuture;
import org.processmining.framework.plugin.Progress;
import org.processmining.framework.plugin.RecursiveCallException;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.events.Logger.MessageLevel;
import org.processmining.framework.plugin.events.ProgressEventListener.ListenerList;
import org.processmining.framework.plugin.impl.FieldSetException;
import org.processmining.framework.plugin.impl.PluginManagerImpl;
import org.processmining.framework.providedobjects.ProvidedObjectManager;
import org.processmining.framework.providedobjects.impl.ProvidedObjectManagerImpl;
import org.processmining.framework.util.Cast;
import org.processmining.framework.util.Pair;

/**
 * For running plugins in headless mode from test scaffold or command line, even
 * if they claim to require UIPluginContext and therefore be GUI only. Adapted
 * from Andreas Rogge-Solti's <code>StochasticNetUtils</code>.
 */

public class ConsoleUIPluginContext implements PluginContext {
	
	private static Logger LOGGER = LogManager.getLogger();
	
    private Progress progress;
    private ProvidedObjectManager objectManager;
    private ConnectionManager connectionManager;

    public ConsoleUIPluginContext() {
        this.progress = new ConsoleProgress();
        this.objectManager = new ProvidedObjectManagerImpl();
        PluginManagerImpl.initialize(PluginContext.class);
        this.connectionManager = new HeadlessConnectionManager();
    }

    public PluginManager getPluginManager() {
        return null;
    }

    public ProvidedObjectManager getProvidedObjectManager() {
        return objectManager;
    }

    public ConnectionManager getConnectionManager() {
        return connectionManager;
    }

    public PluginContextID createNewPluginContextID() {
        return null;
    }

    public void invokePlugin(PluginDescriptor plugin, int index, Object... objects) {
    }

    public void invokeBinding(PluginParameterBinding binding, Object... objects) {
    }

    public Class<? extends PluginContext> getPluginContextType() {
        return null;
    }

    public <T, C extends Connection> Collection<T> tryToFindOrConstructAllObjects(Class<T> type,
                                                                                  Class<C> connectionType, String role, Object... input) throws ConnectionCannotBeObtained {
        return null;
    }

    public <T, C extends Connection> T tryToFindOrConstructFirstObject(Class<T> type, Class<C> connectionType,
                                                                       String role, Object... input) throws ConnectionCannotBeObtained {
        return findOrConstructAllObjects(true, type, null, connectionType, role, input).iterator().next();
    }

    private <T, C extends Connection> Collection<T> findOrConstructAllObjects(boolean stopAtFirst, Class<T> type,
                                                                              String name, Class<C> connectionType, String role, Object... input) throws ConnectionCannotBeObtained {

        Collection<T> accepted = new ArrayList<T>();
        try {
            for (C conn : getConnectionManager().getConnections(connectionType, this, input)) {
                Object object = conn.getObjectWithRole(role);
                if (type.isAssignableFrom(object.getClass())) {
                    accepted.add(Cast.<T>cast(object));
                }
            }
        } catch (Exception e) {
            // Don't care, let's try to construct later
        }
        if (!accepted.isEmpty()) {
            return accepted;
        }
        try {
            return constructAllObjects(stopAtFirst, type, name, input);
        } catch (Exception e) {
            throw new ConnectionCannotBeObtained(e.getMessage(), connectionType);
        }
    }

    private <T, C extends Connection> Collection<T> constructAllObjects(boolean stopAtFirst, Class<T> type,
                                                                        String name, Object... input) throws CancellationException, InterruptedException, ExecutionException {
        Class<?>[] types;
        if (input != null) {
            types = new Class<?>[input.length];
            for (int i = 0; i < input.length; i++) {
                types[i] = input[i].getClass();
            }
        } else {
            types = new Class<?>[0];
            input = new Object[0];
        }

        // Find available plugins
        Set<Pair<Integer, PluginParameterBinding>> set = getPluginManager().find(Plugin.class, type,
                getPluginContextType(), true, false, false, types);

        if (set.isEmpty()) {
            throw new RuntimeException("No plugin available to build this type of object: " + type.toString());
        }

        // Filter on the given name, if given.
        if (name != null) {
            Set<Pair<Integer, PluginParameterBinding>> filteredSet = new HashSet<Pair<Integer, PluginParameterBinding>>();
            for (Pair<Integer, PluginParameterBinding> pair : set) {
                if (name.equals(pair.getSecond().getPlugin().getName())) {
                    filteredSet.add(pair);
                }
            }
            set.clear();
            set.addAll(filteredSet);
        }

        if (set.isEmpty()) {
            throw new RuntimeException("No named plugin available to build this type of object: " + name + ", "
                    + type.toString());
        }

        SortedSet<Pair<Integer, PluginParameterBinding>> plugins = new TreeSet<Pair<Integer, PluginParameterBinding>>(
                new Comparator<Pair<Integer, PluginParameterBinding>>() {

                    public int compare(Pair<Integer, PluginParameterBinding> arg0,
                                       Pair<Integer, PluginParameterBinding> arg1) {
                        int c = arg0.getSecond().getPlugin().getReturnNames().size()
                                - arg1.getSecond().getPlugin().getReturnNames().size();
                        if (c == 0) {
                            c = arg0.getSecond().compareTo(arg1.getSecond());
                        }
                        if (c == 0) {
                            c = arg0.getFirst() - arg1.getFirst();
                        }
                        return c;
                    }

                });
        plugins.addAll(set);

        Collection<T> result = new ArrayList<T>(stopAtFirst ? 1 : plugins.size());

        // get the first available plugin
        ExecutionException ex = null;
        for (Pair<Integer, PluginParameterBinding> pair : plugins) {
            PluginParameterBinding binding = pair.getSecond();
            // create a context to execute this plugin in
            PluginContext child = createChildContext("Computing: " + type.toString());
            getPluginLifeCycleEventListeners().firePluginCreated(child);

            // Invoke the binding
            PluginExecutionResult pluginResult = binding.invoke(child, input);

            // synchronize on the required result and continue
            try {
                pluginResult.synchronize();

                // get all results and pass them to the framework as provided objects
                getProvidedObjectManager().createProvidedObjects(child);
                result.add(pluginResult.<T>getResult(pair.getFirst()));
                if (stopAtFirst) {
                    break;
                }
            } catch (ExecutionException e) {
                // Try next plugin if stop at first, otherwise rethrow
                ex = e;
            } finally {
                child.getParentContext().deleteChild(child);
            }
        }
        if (result.isEmpty()) {
            assert (ex != null);
            throw ex;
        }
        return result;
    }

    public <T, C extends Connection> T tryToFindOrConstructFirstNamedObject(Class<T> type, String name,
                                                                            Class<C> connectionType, String role, Object... input) throws ConnectionCannotBeObtained {
        return null;
    }

    public PluginContext createChildContext(String label) {
        return null;
    }

    public Progress getProgress() {
        return progress;
    }

    public ListenerList getProgressEventListeners() {
        return null;
    }

    public org.processmining.framework.plugin.events.PluginLifeCycleEventListener.List getPluginLifeCycleEventListeners() {
        return null;
    }

    public PluginContextID getID() {
        return null;
    }

    public String getLabel() {
        return null;
    }

    public Pair<PluginDescriptor, Integer> getPluginDescriptor() {
        return null;
    }

    public PluginContext getParentContext() {
        return null;
    }

    public java.util.List<PluginContext> getChildContexts() {
        return null;
    }

    public PluginExecutionResult getResult() {
        return null;
    }

    public ProMFuture<?> getFutureResult(int i) {
        return new ProMFuture<Object>(XLog.class, "name") {

            @Override
            protected Object doInBackground() throws Exception {
                return new Object();
            }
        };
    }

    public Executor getExecutor() {
        return null;
    }

    public boolean isDistantChildOf(PluginContext context) {
        return false;
    }

    public void setFuture(PluginExecutionResult resultToBe) {

    }

    public void setPluginDescriptor(PluginDescriptor descriptor, int methodIndex) throws FieldSetException,
            RecursiveCallException {

    }

    public boolean hasPluginDescriptorInPath(PluginDescriptor descriptor, int methodIndex) {
        return false;
    }

    public void log(String message, MessageLevel level) {
    	LOGGER.log(promToLogLevel(level), message);
    }
    
    private static Level promToLogLevel(MessageLevel level) {
    	Level result = Level.INFO;
    	switch (level){
		case DEBUG:
			result = Level.DEBUG;
			break;
		case ERROR:
			result = Level.ERROR;
			break;
		case NORMAL:
			result = Level.INFO;
			break;
		case TEST:
			result = Level.DEBUG;
			break;
		case WARNING:
			result = Level.WARN;
			break;
    	}
    	return result;
    }

    public void log(String message) {
    	LOGGER.info(message);
    }

    public void log(Throwable exception) {
    	LOGGER.error("Plugin error",exception);
    }

    public org.processmining.framework.plugin.events.Logger.ListenerList getLoggingListeners() {
        return null;
    }

    public PluginContext getRootContext() {
        return null;
    }

    public boolean deleteChild(PluginContext child) {
        return false;
    }

    public <T extends Connection> T addConnection(T c) {
        return connectionManager.addConnection(c);
    }

    public void clear() {
    }

}
