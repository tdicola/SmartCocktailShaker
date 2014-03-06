package com.tonydicola.smartshaker.factories;

import com.tonydicola.smartshaker.interfaces.DrinkProvider;

public enum DrinkFactory {
    INSTANCE;

    private DrinkProvider provider = null;

    public DrinkProvider getProvider() {
        return provider;
    }

    public void setProvider(DrinkProvider provider) {
        this.provider = provider;
    }
}
