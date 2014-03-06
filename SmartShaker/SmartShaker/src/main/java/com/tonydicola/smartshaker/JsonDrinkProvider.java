package com.tonydicola.smartshaker;

import android.content.Context;

import com.tonydicola.smartshaker.interfaces.Drink;
import com.tonydicola.smartshaker.interfaces.DrinkProvider;
import com.tonydicola.smartshaker.interfaces.PreparationStep;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;

public class JsonDrinkProvider implements DrinkProvider {

    private TreeMap<String, JSONObject> drinks = new TreeMap<String, JSONObject>();

    public JsonDrinkProvider(String data) {
        try {
            // Parse JSON data.
            JSONObject json = new JSONObject(data);
            // Load drinks into sorted map in memory.
            JSONArray drinkArray = json.getJSONArray("drinks");
            for (int i = 0; i < drinkArray.length(); ++i) {
                JSONObject drink = drinkArray.getJSONObject(i);
                drinks.put(drink.getString("name"), drink);
            }

        }
        catch (JSONException e) {
            // Failed to parse document, move on with no data loaded.
        }

    }

    @Override
    public Collection<String> getDrinkNames() {
        // Return all the drink names in sorted order.
        return drinks.keySet();
    }

    @Override
    public Drink getDrink(String name) {
        return new JSONDrink(drinks.get(name));
    }

    public static DrinkProvider loadFromResource(Context context, int resourceId) {
        // Load drink JSON data from a resource file.
        try {
            InputStream drinkResource = context.getResources().openRawResource(resourceId);
            String data = IOUtils.toString(drinkResource);
            IOUtils.closeQuietly(drinkResource);
            return new JsonDrinkProvider(data);
        }
        catch (IOException e) {
            // Do nothing, no drink provider loaded.
        }
        return null;
    }

    public class JSONDrink implements Drink {
        private JSONObject drink;
        private ArrayList<PreparationStep> steps = new ArrayList<PreparationStep>();

        public JSONDrink(JSONObject drink) {
            this.drink = drink;
            // Parse preparation steps
            JSONArray stepArray = drink.optJSONArray("preparation");
            if (stepArray != null) {
                for (int i = 0; i < stepArray.length(); ++i) {
                    steps.add(new JSONPreparationStep(stepArray.optJSONObject(i)));
                }
            }
        }

        @Override
        public String getName() {
            return drink.optString("name");
        }

        @Override
        public String getDescription() {
            return drink.optString("description");
        }

        @Override
        public List<PreparationStep> getPreparation() {
            return steps;
        }
    }

    public class JSONPreparationStep implements PreparationStep {
        private JSONObject step;

        public JSONPreparationStep(JSONObject step) {
            this.step = step;
        }

        @Override
        public String getInstructions() {
            return step.optString("instructions");
        }

        @Override
        public Double getMeasureGrams() {
            try {
                return step.getDouble("measure_grams");
            }
            catch (JSONException e) {
                // No measure value, return null.
                return null;
            }
        }

        @Override
        public Double getGramsPerOz() {
            try {
                return step.getDouble("grams_per_oz");
            }
            catch (JSONException e) {
                // No measure value, return null.
                return null;
            }
        }
    }
}
