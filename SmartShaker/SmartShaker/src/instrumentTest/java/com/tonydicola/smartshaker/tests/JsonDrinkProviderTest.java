package com.tonydicola.smartshaker.tests;

import android.test.AndroidTestCase;

import com.tonydicola.smartshaker.JsonDrinkProvider;
import com.tonydicola.smartshaker.interfaces.Drink;
import com.tonydicola.smartshaker.interfaces.PreparationStep;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JsonDrinkProviderTest extends AndroidTestCase {
    public void test_getDrinkNames_returns_names_sorted() {
        JsonDrinkProvider drinks = new JsonDrinkProvider("{ 'drinks': [ { 'name': 'Old Fashioned' }, { 'name': 'Bloody Mary' }, { 'name': 'Manhattan' } ] }");

        ArrayList<String> names = new ArrayList<String>(drinks.getDrinkNames());

        assertEquals(names, Arrays.asList("Bloody Mary", "Manhattan", "Old Fashioned"));
    }

    public void test_getDrink_returns_expected_drink() {
        JsonDrinkProvider drinks = new JsonDrinkProvider("{ 'drinks': [ { 'name': 'Old Fashioned', 'description': 'Good.' }, { 'name': 'Bloody Mary', 'description': 'Also good.' } ] }");

        Drink drink = drinks.getDrink("Old Fashioned");

        assertEquals("Old Fashioned", drink.getName());
        assertEquals("Good.", drink.getDescription());
    }

    public void test_getPreparation_returns_steps_in_order() {
        JsonDrinkProvider drinks = new JsonDrinkProvider("{ 'drinks': [ { 'name': 'Old Fashioned', 'preparation': [ { 'instructions': 'One', 'measure_grams': '1' }, { 'instructions': 'Two', 'measure_grams': '2' }, { 'instructions': 'Three' } ] } ] }");

        Drink drink = drinks.getDrink("Old Fashioned");
        List<PreparationStep> steps = drink.getPreparation();

        assertEquals(3, steps.size());
        assertEquals("One", steps.get(0).getInstructions());
        assertEquals("Two", steps.get(1).getInstructions());
        assertEquals("Three", steps.get(2).getInstructions());
        assertEquals(1.0, steps.get(0).getMeasureGrams());
        assertEquals(2.0, steps.get(1).getMeasureGrams());
        assertNull(steps.get(2).getMeasureGrams());
    }
}

