package ru.myproject.cadence.cadence.workflow.impl;

import com.uber.cadence.activity.ActivityOptions;
import com.uber.cadence.workflow.Async;
import com.uber.cadence.workflow.Promise;
import com.uber.cadence.workflow.Workflow;
import ru.myproject.cadence.cadence.activity.ShowElementsActivity;
import ru.myproject.cadence.cadence.workflow.GetListElementsWorkflow;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class GetListElementsWorkflowImpl implements GetListElementsWorkflow {

    private final ActivityOptions activityOptions = new ActivityOptions
            .Builder()
            .setScheduleToCloseTimeout(Duration.ofMinutes(1))
            .build();

    private final ShowElementsActivity showElementsActivity =
            Workflow.newActivityStub(ShowElementsActivity.class, activityOptions);

    @Override
    public void getListElements(List<String> strings) {

        List<Promise<Void>> promises = new ArrayList<>();

        strings.forEach(string -> {
            Promise<Void> promise = Async.function(showElementsActivity::showElements, string);
            promises.add(promise);
        });

        promises.forEach(Promise::get);
    }
}
