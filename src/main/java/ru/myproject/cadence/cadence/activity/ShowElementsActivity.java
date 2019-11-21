package ru.myproject.cadence.cadence.activity;

import com.uber.cadence.activity.ActivityMethod;

public interface ShowElementsActivity {

    @ActivityMethod(scheduleToCloseTimeoutSeconds = 300, taskList = "TASK_LIST")
    void showElements(String element);
}
