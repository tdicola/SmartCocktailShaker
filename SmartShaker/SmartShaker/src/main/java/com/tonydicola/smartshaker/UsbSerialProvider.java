package com.tonydicola.smartshaker;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.UsbSerialInputStream;
import com.hoho.android.usbserial.util.UsbSerialOutputStream;
import com.tonydicola.smartshaker.interfaces.ConnectionProvider;
import com.tonydicola.smartshaker.interfaces.DeviceConnection;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class UsbSerialProvider implements ConnectionProvider {

    private Context context;
    private UsbManager manager;

    public UsbSerialProvider(Context context) {
        this.context = context;
        manager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
    }

    @Override
    public List<DeviceConnection> getConnections() {
        // Get all the supported USB serial devices and return them.
        ArrayList<DeviceConnection> connections = new ArrayList<DeviceConnection>();
        for (UsbDevice device : UsbSerialProber.findSupportedDevices(manager)) {
            connections.add(new Connection(context, manager, device));
        }
        return connections;
    }

    public class Connection implements DeviceConnection {

        private Context context;
        private UsbManager manager;
        private UsbDevice device;
        private UsbSerialDriver driver;
        private Runnable granted;
        private BufferedReader input;
        private BufferedWriter output;

        private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

        public Connection(Context context, UsbManager manager, UsbDevice device) {
            this.context = context;
            this.manager = manager;
            this.device = device;
        }

        private final BroadcastReceiver receiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                // Switch to measurement activity if permission is granted.
                if (!(ACTION_USB_PERMISSION.equals(intent.getAction()) &&
                      intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false))) {
                    return;
                }
                // Get the device from the intent.
                device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                // Stop listening for receiver permission events.
                context.unregisterReceiver(receiver);
                // Notify that permission has been granted to the device.
                if (granted != null) {
                    granted.run();
                }
            }
        };

        @Override
        public void requestPermission(Runnable granted) {
            this.granted = granted;
            // Register USB broadcast event receiver.
            context.registerReceiver(receiver, new IntentFilter(ACTION_USB_PERMISSION));
            // Ask for permission to the device.
            manager.requestPermission(device, PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0));
        }

        @Override
        public void open() throws IOException {
            // Get the UsbSerialDriver instance for this USB device.
            List<UsbSerialDriver> drivers = UsbSerialProber.probeSingleDevice(manager, device);
            if (drivers.size() < 1) throw new IOException("Could not create UsbSerialDriver for device.");
            driver = drivers.get(0);
            // Open the connection using 9600 baud.
            driver.setParameters(9600, 8, 1, 0);
            driver.open();
            // Initialize the reader and writer for reading and writing over USB.
            input = new BufferedReader(new InputStreamReader(new UsbSerialInputStream(driver)));
            output = new BufferedWriter(new OutputStreamWriter(new UsbSerialOutputStream(driver)));
        }

        @Override
        public Double getMeasure() throws IOException {
            // Fail if the driver is closed.
            if (driver == null || input == null || output == null) throw new IOException("Device not open.");
            // Write a question mark.
            output.write("?");
            output.flush();
            // Receive a line in response.
            String line = input.readLine();
            // Parse a double from the response and return it.
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
                driver.close();
                driver = null;
                input = null;
                output = null;
            }
            catch (IOException e) {
                // Exception closing device.  Do nothing.
            }
        }

        @Override
        public String toString() {
            return device.getDeviceName();
        }
    }
}
