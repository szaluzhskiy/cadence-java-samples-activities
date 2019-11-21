package ru.myproject.cadence.cadence.workflow;

import com.uber.cadence.workflow.WorkflowMethod;
import java.util.List;

public interface GetListElementsWorkflow {

    @WorkflowMethod(executionStartToCloseTimeoutSeconds = 600, taskList = "TASK_LIST")
    void getListElements(List<String> strings);
}
