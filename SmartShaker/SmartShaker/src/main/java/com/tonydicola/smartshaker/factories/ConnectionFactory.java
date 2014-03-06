package com.tonydicola.smartshaker.factories;

import com.tonydicola.smartshaker.interfaces.ConnectionProvider;
import com.tonydicola.smartshaker.interfaces.DeviceConnection;

import java.util.ArrayList;
import java.util.List;

public enum ConnectionFactory {
    INSTANCE;

    private DeviceConnection connection = null;
    private ArrayList<ConnectionProvider> sources = new ArrayList<ConnectionProvider>();

    public DeviceConnection getConnection() {
        return connection;
    }

    public void setConnection(DeviceConnection connection) {
        this.connection = connection;
    }

    public List<DeviceConnection> getConnections() {
        // Return a list of all connections from all sources.
        ArrayList<DeviceConnection> devices = new ArrayList<DeviceConnection>();
        for (ConnectionProvider source : sources) {
            devices.addAll(source.getConnections());
        }
        return devices;
    }

    public void addSource(ConnectionProvider source) {
        sources.add(source);
    }

}
