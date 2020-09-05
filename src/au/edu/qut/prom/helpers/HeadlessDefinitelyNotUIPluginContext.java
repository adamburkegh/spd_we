package au.edu.qut.prom.helpers;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.Executor;

import javax.swing.JComponent;
import javax.swing.filechooser.FileFilter;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.processmining.contexts.uitopia.UIContext;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.model.ProMTask;
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
import org.processmining.framework.plugin.events.Logger.MessageLevel;
import org.processmining.framework.plugin.events.PluginLifeCycleEventListener.List;
import org.processmining.framework.plugin.events.ProgressEventListener.ListenerList;
import org.processmining.framework.plugin.impl.FieldSetException;
import org.processmining.framework.providedobjects.ProvidedObjectManager;
import org.processmining.framework.util.Pair;

/**
 * Usually it's enough for the type signature to be a PluginContext. But some code specifically
 * checks if the object is an instanceof UIPluginContext, and then starts throwing dialog boxes
 * up all over your nice new shoes. 
 * 
 * @author burkeat
 *
 */
public class HeadlessDefinitelyNotUIPluginContext implements PluginContext{

	private HeadlessUIPluginContext delegate;
	
	public HeadlessDefinitelyNotUIPluginContext(PluginContext context, String label) {
		delegate = new HeadlessUIPluginContext (context,label);
	}

	public int hashCode() {
		return delegate.hashCode();
	}

	public ConnectionManager getConnectionManager() {
		return delegate.getConnectionManager();
	}

	public <T extends Connection> T addConnection(T c) {
		return delegate.addConnection(c);
	}

	public void clear() {
		delegate.clear();
	}

	public Progress getProgress() {
		return delegate.getProgress();
	}

	public ProMFuture<?> getFutureResult(int i) {
		return delegate.getFutureResult(i);
	}

	public void setFuture(PluginExecutionResult futureToBe) {
		delegate.setFuture(futureToBe);
	}

	public PluginExecutionResult getResult() {
		return delegate.getResult();
	}

	public Executor getExecutor() {
		return delegate.getExecutor();
	}

	public void log(String message, MessageLevel level) {
		delegate.log(message, level);
	}

	public void log(String message) {
		delegate.log(message);
	}

	public void log(Throwable exception) {
		delegate.log(exception);
	}

	public File openFile(FileFilter filter) throws IOException {
		return delegate.openFile(filter);
	}

	public File saveFile(String defaultExtension, String... extensions) throws IOException {
		return delegate.saveFile(defaultExtension, extensions);
	}

	public File[] openFiles(FileFilter filter) throws IOException {
		return delegate.openFiles(filter);
	}

	public Pair<PluginDescriptor, Integer> getPluginDescriptor() {
		return delegate.getPluginDescriptor();
	}

	public UIContext getGlobalContext() {
		return delegate.getGlobalContext();
	}

	public List getPluginLifeCycleEventListeners() {
		return delegate.getPluginLifeCycleEventListeners();
	}

	public UIPluginContext getRootContext() {
		return delegate.getRootContext();
	}

	public ListenerList getProgressEventListeners() {
		return delegate.getProgressEventListeners();
	}

	public void setTask(ProMTask task) {
		delegate.setTask(task);
	}

	public ProMTask getTask() {
		return delegate.getTask();
	}

	public PluginContextID getID() {
		return delegate.getID();
	}

	public InteractionResult showConfiguration(String title, JComponent configuration) {
		return delegate.showConfiguration(title, configuration);
	}

	public String getLabel() {
		return delegate.getLabel();
	}

	public boolean hasPluginDescriptorInPath(PluginDescriptor plugin, int methodIndex) {
		return delegate.hasPluginDescriptorInPath(plugin, methodIndex);
	}

	public InteractionResult showWizard(String title, boolean first, boolean last, JComponent configuration) {
		return delegate.showWizard(title, first, last, configuration);
	}

	public void setPluginDescriptor(PluginDescriptor descriptor, int methodIndex)
			throws FieldSetException, RecursiveCallException {
		delegate.setPluginDescriptor(descriptor, methodIndex);
	}

	public UIPluginContext createChildContext(String label) {
		return delegate.createChildContext(label);
	}

	public java.util.List<PluginContext> getChildContexts() {
		return delegate.getChildContexts();
	}

	public PluginContext getParentContext() {
		return delegate.getParentContext();
	}

	public boolean equals(Object o) {
		return delegate.equals(o);
	}

	public String toString() {
		return delegate.toString();
	}

	public boolean isDistantChildOf(PluginContext context) {
		return delegate.isDistantChildOf(context);
	}

	public PluginManager getPluginManager() {
		return delegate.getPluginManager();
	}

	public ProvidedObjectManager getProvidedObjectManager() {
		return delegate.getProvidedObjectManager();
	}

	public PluginContextID createNewPluginContextID() {
		return delegate.createNewPluginContextID();
	}

	public void invokePlugin(PluginDescriptor plugin, int index, Object... objects) {
		delegate.invokePlugin(plugin, index, objects);
	}

	public void invokeBinding(PluginParameterBinding binding, Object... objects) {
		delegate.invokeBinding(binding, objects);
	}

	public org.processmining.framework.plugin.events.Logger.ListenerList getLoggingListeners() {
		return delegate.getLoggingListeners();
	}

	public boolean deleteChild(PluginContext child) {
		return delegate.deleteChild(child);
	}

	public Class<? extends PluginContext> getPluginContextType() {
		return delegate.getPluginContextType();
	}

	public <T, C extends Connection> Collection<T> tryToFindOrConstructAllObjects(Class<T> type,
			Class<C> connectionType, String role, Object... input) throws ConnectionCannotBeObtained {
		return delegate.tryToFindOrConstructAllObjects(type, connectionType, role, input);
	}

	public <T, C extends Connection> T tryToFindOrConstructFirstObject(Class<T> type, Class<C> connectionType,
			String role, Object... input) throws ConnectionCannotBeObtained {
		return delegate.tryToFindOrConstructFirstObject(type, connectionType, role, input);
	}

	public <T, C extends Connection> T tryToFindOrConstructFirstNamedObject(Class<T> type, String name,
			Class<C> connectionType, String role, Object... input) throws ConnectionCannotBeObtained {
		return delegate.tryToFindOrConstructFirstNamedObject(type, name, connectionType, role, input);
	}


}
