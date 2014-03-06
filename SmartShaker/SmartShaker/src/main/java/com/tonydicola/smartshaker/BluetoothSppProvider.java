package com.tonydicola.smartshaker;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import com.tonydicola.smartshaker.interfaces.ConnectionProvider;
import com.tonydicola.smartshaker.interfaces.DeviceConnection;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BluetoothSppProvider implements ConnectionProvider {

    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothAdapter adapter;

    public BluetoothSppProvider() {
        adapter = BluetoothAdapter.getDefaultAdapter();
    }

    @Override
    public List<DeviceConnection> getConnections() {
        // Return a list of all the paired bluetooth devices.
        ArrayList<DeviceConnection> connections = new ArrayList<DeviceConnection>();
        if (adapter != null && adapter.isEnabled()) {
            for (BluetoothDevice device : adapter.getBondedDevices()) {
                connections.add(new Connection(device));
            }
        }
        return connections;
    }

    public class Connection implements DeviceConnection {

        private BluetoothDevice device;
        private BluetoothSocket socket;
        private BufferedReader input;
        private BufferedWriter output;

        public Connection(BluetoothDevice device) {
            this.device = device;
        }

        @Override
        public void requestPermission(Runnable granted) {
            // No need to wait for permission since the device is already paired.
            granted.run();
        }

        @Override
        public void open() throws IOException {
            // Cancel discovery before attempting to a connection.
            BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
            // Create bluetooth socket and connect.
            socket = device.createRfcommSocketToServiceRecord(SPP_UUID);
            socket.connect();
            // Initialize reader and writer for communicating with device.
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        }

        @Override
        public Double getMeasure() throws IOException {
            // Fail if the device isn't open.
            if (socket == null || input == null || output == null) throw new IOException("Device not open.");
            // Write a question mark.
            output.write("?");
            output.flush();
            // Read a line of input.
            String line = input.readLine();
            // Parse a double value from the input and return it as the measurement.
            if (line != null && !line.isEmpty()) {
                return Double.parseDouble(line.trim());
            }
            else {
                return null;
            }
        }

        @Override
        public void close() {
            try {
                socket.close();
                socket = null;
                input = null;
                output = null;
            }
            catch (IOException exception) {
                // Do nothing on error trying to close.
            }
        }

        @Override
        public String toString() {
            return device.getName();
        }
    }
}
