package ru.myproject.cadence.cadence.activity.impl;

import lombok.extern.slf4j.Slf4j;
import ru.myproject.cadence.cadence.activity.ShowElementsActivity;

@Slf4j
public class ShowElementsActivityImpl implements ShowElementsActivity {

    @Override
    public Void showElements(String element) {

        log.info(element);

        return null;
    }
}
