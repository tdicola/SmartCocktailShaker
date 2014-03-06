package com.tonydicola.smartshaker.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.tonydicola.smartshaker.factories.ConnectionFactory;
import com.tonydicola.smartshaker.interfaces.DeviceConnection;
import com.tonydicola.smartshaker.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ChooseConnection extends Activity {

    @InjectView(R.id.chooseconnection_device_names)
    public Spinner deviceNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_connection);

        // Inject members.
        ButterKnife.inject(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshDeviceNames();
    }

    public void refreshDeviceNames() {
        deviceNames.setAdapter(new ArrayAdapter<DeviceConnection>(
                this,
                android.R.layout.simple_list_item_1,
                ConnectionFactory.INSTANCE.getConnections()
        ));
    }

    public void refresh(View view) {
        refreshDeviceNames();
    }

    public void select(View view) {
        // Get selected device.
        final DeviceConnection device = (DeviceConnection) deviceNames.getSelectedItem();
        if (device != null) {
            // Request permission to the device.
            device.requestPermission(new Runnable() {
                @Override
                public void run() {
                    // When permission is granted, save the connection and finish the activity.
                    ConnectionFactory.INSTANCE.setConnection(device);
                    finish();
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.connection, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return super.onOptionsItemSelected(item);
    }

}
