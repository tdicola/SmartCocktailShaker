package com.tonydicola.smartshaker;

import com.tonydicola.smartshaker.interfaces.Drink;
import com.tonydicola.smartshaker.interfaces.PreparationStep;

public class PrepareDrinkModel {

    private Drink drink;
    private int currentStep;
    private double measureGrams;
    private Double tareGrams;

    public PrepareDrinkModel(Drink drink) {
        this.drink = drink;
        startOver();
    }

    public void nextStep() {
        // Advance to the next step if not at the end, and reset all the appropriate state.
        if (currentStep < (drink.getPreparation().size() - 1)) {
            currentStep += 1;
            tareGrams = null;
            measureGrams = 0;
        }
    }

    public void startOver() {
        currentStep = 0;
        measureGrams = 0;
        tareGrams = null;
    }

    public int getCurrentStep() {
        return currentStep;
    }

    public boolean isCurrentStep(int number) {
        return currentStep == number;
    }

    public boolean stepHasMeasurement(int number) {
        // Return true or false if the requested step has a measurement or not.
        PreparationStep step = drink.getPreparation().get(number);
        return (step.getMeasureGrams() != null && step.getGramsPerOz() != null);
    }

    public double getMeasureGrams() {
        return measureGrams;
    }

    public double getMeasureOz() {
        // Convert current measure from weight to volume using the density of the liquid in the current step.
        Double density = drink.getPreparation().get(currentStep).getGramsPerOz();
        if (density != null) {
            return measureGrams / density;
        }
        else {
            return 0;
        }
    }

    public int getMeasureProgress() {
        // Return a value from 0 to 100 based on what percent of the current step's measure is applied to the scale.
        Double total = drink.getPreparation().get(currentStep).getMeasureGrams();
        if (total != null) {
            return (int)(measureGrams / total * 100.0);
        }
        else {
            return 0;
        }
    }

    public void updateMeasure(double rawGrams) {
        // Don't make any updates if the current step has no measure defined.
        if (!stepHasMeasurement(currentStep)) return;
        // Use the first measurement to tare future measures.
        if (tareGrams == null) {
            tareGrams = rawGrams;
        }
        // Else set the current measurement to the measured value minus the tare value.
        else {
            measureGrams = rawGrams - tareGrams;
            measureGrams = measureGrams < 0.0 ? 0.0 : measureGrams;
        }
    }
}
