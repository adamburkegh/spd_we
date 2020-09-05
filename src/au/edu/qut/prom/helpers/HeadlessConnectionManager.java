package au.edu.qut.prom.helpers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.processmining.framework.connections.Connection;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.connections.ConnectionID;
import org.processmining.framework.connections.ConnectionManager;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.events.ConnectionObjectListener;


/**
 * For running plugins in headless mode from test scaffold or command line, even if they claim to 
 * require UIPluginContext and therefore
 * be GUI only. Adapted from Andreas Rogge-Solti's <code>StochasticNetUtils</code>
 * 
 */
public class HeadlessConnectionManager implements ConnectionManager {
	
    private final Map<ConnectionID, Connection> connections = new HashMap<ConnectionID, Connection>();

    public HeadlessConnectionManager() {
    }

    public void setEnabled(boolean isEnabled) {
    }

    public boolean isEnabled() {
        return false;
    }

	public <T extends Connection> T getFirstConnection(Class<T> connectionType, PluginContext context,
                                                       Object... objects) throws ConnectionCannotBeObtained 
    {
    	// Leemans just refuses to provide connections and it seems to go smoother
        throw new ConnectionCannotBeObtained("Connections aren't provided during headless run", connectionType,
                objects);
        // Rogge-Solti tries to maintain connections and it causes NPEs when they are marked removed and 
        // fall out of memory - see method of this name in StochasticNetUtils 
    }

	public <T extends Connection> Collection<T> getConnections(Class<T> connectionType, PluginContext context,
                                                               Object... objects) throws ConnectionCannotBeObtained {
        throw new ConnectionCannotBeObtained("Connections aren't provided during headless run", connectionType,
                objects);
    }

    public org.processmining.framework.plugin.events.ConnectionObjectListener.ListenerList getConnectionListeners() {
        org.processmining.framework.plugin.events.ConnectionObjectListener.ListenerList list = new ConnectionObjectListener.ListenerList();
        return list;
    }

    public Collection<ConnectionID> getConnectionIDs() {
        java.util.List<ConnectionID> list = new ArrayList<>();
        return list;
    }

    public Connection getConnection(ConnectionID id) throws ConnectionCannotBeObtained {
        if (connections.containsKey(id)) {
            return connections.get(id);
        }
        throw new ConnectionCannotBeObtained("No connection with id " + id.toString(), null);
    }

    public void clear() {
        this.connections.clear();
    }

    public <T extends Connection> T addConnection(T connection) {
        connections.put(connection.getID(), connection);
        connection.setManager(this);
        return connection;
    }

}


