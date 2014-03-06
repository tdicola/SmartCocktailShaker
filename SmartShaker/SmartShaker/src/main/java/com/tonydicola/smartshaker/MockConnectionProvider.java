package com.tonydicola.smartshaker;

import com.tonydicola.smartshaker.interfaces.ConnectionProvider;
import com.tonydicola.smartshaker.interfaces.DeviceConnection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MockConnectionProvider implements ConnectionProvider {

    @Override
    public List<DeviceConnection> getConnections() {
        ArrayList<DeviceConnection> devices = new ArrayList<DeviceConnection>();
        devices.add(new Connection("Foo"));
        devices.add(new Connection("Bar"));
        return devices;
    }

    public class Connection implements DeviceConnection {
        private String name;

        public Connection(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public void open() throws IOException {
            // Do nothing
        }

        @Override
        public void close() {
            // Do nothing
        }

        @Override
        public Double getMeasure() throws IOException {
            return 0.0;
        }

        @Override
        public void requestPermission(Runnable granted) {
            granted.run();
        }
    }


}
