package ru.myproject.cadence.cadence.activity.impl;

import ru.myproject.cadence.cadence.activity.ShowElementsActivity;

public class ShowElementsActivityImpl implements ShowElementsActivity {

    @Override
    public void showElements(String element) {
        System.out.println("!!! ACTIVITY CALLED !!!");
    }
}
