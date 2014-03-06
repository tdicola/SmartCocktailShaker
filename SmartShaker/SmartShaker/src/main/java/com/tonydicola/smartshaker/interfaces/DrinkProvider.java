package com.tonydicola.smartshaker.interfaces;

import java.util.Collection;

/**
 * Created by Tony on 2/22/14.
 */
public interface DrinkProvider {
    public Collection<String> getDrinkNames();
    public Drink getDrink(String name);
}
