package ru.myproject.cadence.cadence.activity;

import com.uber.cadence.activity.ActivityMethod;

public interface ShowElementsActivity {

    @ActivityMethod(scheduleToCloseTimeoutSeconds = 1200, taskList = "TASK_LIST")
    Void showElements(String element);
}
