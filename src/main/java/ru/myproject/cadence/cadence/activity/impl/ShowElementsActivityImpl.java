package ru.myproject.cadence.cadence.activity.impl;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.myproject.cadence.cadence.activity.ShowElementsActivity;

public class ShowElementsActivityImpl implements ShowElementsActivity {

    @Override
    public void showElements(String element) {
        System.out.println("!!! ACTIVITY CALLED !!!");
    }
}
