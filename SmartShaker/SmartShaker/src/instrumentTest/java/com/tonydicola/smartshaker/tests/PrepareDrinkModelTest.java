package com.tonydicola.smartshaker.tests;

import android.test.AndroidTestCase;

import com.tonydicola.smartshaker.JsonDrinkProvider;
import com.tonydicola.smartshaker.PrepareDrinkModel;
import com.tonydicola.smartshaker.interfaces.DrinkProvider;

public class PrepareDrinkModelTest extends AndroidTestCase {

    private DrinkProvider drinks;

    @Override
    protected void setUp() {
        drinks = new JsonDrinkProvider("{ 'drinks': [ { 'name': 'one', 'preparation': [ {'instructions': 'one'}, {'instructions': 'two', 'measure_grams': '20.0', 'grams_per_oz': '10.0' }, {'instructions': 'three', 'measure_grams': '50.0', 'grams_per_oz': '10.0' } ] } ] }");
    }

    public void test_next_moves_to_next_step() {
        PrepareDrinkModel model = new PrepareDrinkModel(drinks.getDrink("one"));

        assertTrue(model.isCurrentStep(0));

        model.nextStep();

        assertTrue(model.isCurrentStep(1));
    }

    public void test_next_stops_at_last_step() {
        PrepareDrinkModel model = new PrepareDrinkModel(drinks.getDrink("one"));

        model.nextStep();
        model.nextStep();
        model.nextStep();

        assertTrue(model.isCurrentStep(2));
    }

    public void test_startOver_starts_at_first_step() {
        PrepareDrinkModel model = new PrepareDrinkModel(drinks.getDrink("one"));

        model.nextStep();
        model.startOver();

        assertTrue(model.isCurrentStep(0));
    }

    public void test_isCurrentStep_returns_false_for_non_current_step() {
        PrepareDrinkModel model = new PrepareDrinkModel(drinks.getDrink("one"));

        assertFalse(model.isCurrentStep(1));

        model.nextStep();

        assertFalse(model.isCurrentStep(0));
    }

    public void test_getMeasureGrams_returns_zero_at_start_of_new_step() {
        PrepareDrinkModel model = new PrepareDrinkModel(drinks.getDrink("one"));

        model.nextStep();
        model.updateMeasure(100.0);
        model.updateMeasure(120.0);
        model.nextStep();

        assertEquals(0.0, model.getMeasureGrams());
    }

    public void test_measure_not_updated_when_step_has_no_measure() {
        PrepareDrinkModel model = new PrepareDrinkModel(drinks.getDrink("one"));

        assertEquals(0.0, model.getMeasureGrams());

        model.updateMeasure(100.0);
        model.updateMeasure(120.0);

        assertEquals(0.0, model.getMeasureGrams());
    }

    public void test_tare_after_first_update() {
        PrepareDrinkModel model = new PrepareDrinkModel(drinks.getDrink("one"));
        model.nextStep();

        model.updateMeasure(100.0);

        assertEquals(0.0, model.getMeasureGrams());

        model.updateMeasure(120.0);

        assertEquals(20.0, model.getMeasureGrams());
    }

    public void test_tare_again_after_next_step() {
        PrepareDrinkModel model = new PrepareDrinkModel(drinks.getDrink("one"));
        model.nextStep();

        model.updateMeasure(100.0);
        model.updateMeasure(120.0);

        model.nextStep();

        model.updateMeasure(30.0);

        assertEquals(0.0, model.getMeasureGrams());

        model.updateMeasure(40.0);

        assertEquals(10.0, model.getMeasureGrams());
    }

    public void test_update_below_tare_is_zero() {
        PrepareDrinkModel model = new PrepareDrinkModel(drinks.getDrink("one"));
        model.nextStep();

        model.updateMeasure(100.0);

        assertEquals(0.0, model.getMeasureGrams());

        model.updateMeasure(90.0);

        assertEquals(0.0, model.getMeasureGrams());
    }

    public void test_getMeasureOz_converts_measure_to_oz() {
        PrepareDrinkModel model = new PrepareDrinkModel(drinks.getDrink("one"));
        model.nextStep();

        model.updateMeasure(100.0);

        assertEquals(0.0, model.getMeasureOz());

        model.updateMeasure(120.0);

        assertEquals(2.0, model.getMeasureOz());
    }

    public void test_getMeasureProgress_returns_percent_full() {
        PrepareDrinkModel model = new PrepareDrinkModel(drinks.getDrink("one"));
        model.nextStep();

        model.updateMeasure(100.0);

        assertEquals(0, model.getMeasureProgress());

        model.updateMeasure(110.0);

        assertEquals(50, model.getMeasureProgress());

        model.updateMeasure(120.0);

        assertEquals(100, model.getMeasureProgress());
    }

}
