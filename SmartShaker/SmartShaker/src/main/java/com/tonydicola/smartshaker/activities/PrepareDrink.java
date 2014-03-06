package com.tonydicola.smartshaker.activities;

import android.app.Activity;
import android.bluetooth.BluetoothClass;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tonydicola.smartshaker.PrepareDrinkModel;
import com.tonydicola.smartshaker.factories.ConnectionFactory;
import com.tonydicola.smartshaker.factories.DrinkFactory;
import com.tonydicola.smartshaker.interfaces.DeviceConnection;
import com.tonydicola.smartshaker.R;
import com.tonydicola.smartshaker.StepListAdapter;
import com.tonydicola.smartshaker.interfaces.Drink;
import com.tonydicola.smartshaker.interfaces.DrinkProvider;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class PrepareDrink extends Activity {

    public static final String DRINK_NAME = "com.tonydicola.smartshaker.DRINK_NAME";

    public static long measurePeriodMillis = 250;

    @InjectView(R.id.preparedrink_steps)
    public ListView stepListView;

    @InjectView(R.id.preparedrink_main)
    public RelativeLayout mainView;

    @InjectView(R.id.preparedrink_connecting)
    public TextView connectingView;

    private PrepareDrinkModel model;

    private StepListAdapter stepListAdapter;

    private DeviceConnection connection;

    private AtomicLong measureData = new AtomicLong();

    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prepare_drink);

        // Inject members.
        ButterKnife.inject(this);

        DrinkProvider drinks = DrinkFactory.INSTANCE.getProvider();

        // Get the drink name passed to this activity.
        Intent intent = getIntent();
        String drinkName = intent.getStringExtra(DRINK_NAME);

        // Stop if something is wrong and drink data isn't available.
        if (drinks == null) finish();
        Drink drink = drinks.getDrink(drinkName);
        if (drink == null) finish();

        // Populate the view with values for the specified drink.
        model = new PrepareDrinkModel(drink);
        stepListAdapter = new StepListAdapter(
                this,
                drink.getPreparation(),
                model,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        nextClick();
                    }
                }
        );
        stepListView.setAdapter(stepListAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        connection = ConnectionFactory.INSTANCE.getConnection();
        if (connection == null) {
            // No selected connection, redirect to choose connection activity.
            startActivity(new Intent(this, ChooseConnection.class));
        }
        else {
            // Show the connecting view.
            mainView.setVisibility(View.GONE);
            connectingView.setVisibility(View.VISIBLE);
            // Set up timer (marked as a daemon so it doesn't block quitting).
            timer = new Timer(true);
            // Open the device connection immediately on the timer's thread.
            // This is done so the connection open doesn't block the main UI thread.
            timer.schedule(new OpenConnection(), 0);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Stop all tasks on the timer thread and destroy it.
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        // Close the device connection.
        // This really should be done in a separate thread, but for simplicity it will be closed here.
        if (connection != null) {
            connection.close();
        }
    }

    public void nextClick() {
        model.nextStep();
        stepListAdapter.notifyDataSetChanged();
    }

    public void startOverClick(View view) {
        model.startOver();
        stepListAdapter.notifyDataSetChanged();
    }

    // Handler to update the UI when a new measurement is read from the device.
    public final Handler measureUpdate = new Handler() {
        @Override
        public void handleMessage(Message message) {
            model.updateMeasure(Double.longBitsToDouble(measureData.get()));
            stepListAdapter.notifyDataSetChanged();
        }
    };

    private class OpenConnection extends TimerTask {
        @Override
        public void run() {
            try {
                // Open device connection.
                connection.open();
                // Schedule a regular task to get a new measurement and update the UI.
                timer.scheduleAtFixedRate(new UpdateMeasurement(), 0, measurePeriodMillis);
                // Show the main view (must be run on UI thread!).
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mainView.setVisibility(View.VISIBLE);
                        connectingView.setVisibility(View.GONE);
                    }
                });
            }
            catch (IOException e) {
                // Do nothing when connection fails.
            }
        }
    }

    private class UpdateMeasurement extends TimerTask {
        @Override
        public void run() {
            // Check if the current step should have a measurement.
            if (model.stepHasMeasurement(model.getCurrentStep())) {
                try {
                    // Get a new measurement.
                    Double measure = connection.getMeasure();
                    if (measure != null) {
                        // If there's measurement data notify the UI thread that is should update.
                        // Update an AtomicLong with the encoded double data so the UI will always get
                        // the most recent measurement instead of getting an old measure from the handler.
                        measureData.set(Double.doubleToLongBits(measure));
                        measureUpdate.sendEmptyMessage(0);
                    }
                }
                catch (IOException e) {
                    // Do nothing when a measurement can't be read.
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.prepare_drink, menu);
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
