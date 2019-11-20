package ru.myproject.cadence.cadence.workflow;

import com.uber.cadence.workflow.WorkflowMethod;

import java.util.List;

public interface GetListElementsWorkflow {

    @WorkflowMethod
    void getListElements(List<String> strings);
}
