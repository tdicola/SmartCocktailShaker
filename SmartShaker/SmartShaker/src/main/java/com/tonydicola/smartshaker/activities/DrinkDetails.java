package com.tonydicola.smartshaker.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.tonydicola.smartshaker.R;
import com.tonydicola.smartshaker.factories.DrinkFactory;
import com.tonydicola.smartshaker.interfaces.Drink;
import com.tonydicola.smartshaker.interfaces.DrinkProvider;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class DrinkDetails extends Activity {

    public static final String DRINK_NAME = "drink_name";

    @InjectView(R.id.drinkdetail_drink)
    public TextView drinkTextView;

    @InjectView(R.id.drinkdetail_description)
    public TextView descriptionTextView;

    private Drink drink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drink_details);

        // Inject members.
        ButterKnife.inject(this);

        DrinkProvider drinks = DrinkFactory.INSTANCE.getProvider();

        // Get the drink name passed to this activity.
        Intent intent = getIntent();
        String drinkName = intent.getStringExtra(DRINK_NAME);

        // Populate UI with drink data.
        if (drinks == null) return;

        drink = drinks.getDrink(drinkName);
        if (drink != null) {
            drinkTextView.setText(drink.getName());
            descriptionTextView.setText(Html.fromHtml(drink.getDescription()));
            descriptionTextView.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

    public void prepareDrink(View view) {
        if (drink == null) return;
        Intent intent = new Intent(this, PrepareDrink.class);
        intent.putExtra(PrepareDrink.DRINK_NAME, drink.getName());
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.drink_details, menu);
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
