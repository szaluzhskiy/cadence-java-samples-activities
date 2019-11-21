package ru.myproject.cadence.service;

import com.uber.cadence.client.WorkflowClient;
import com.uber.cadence.client.WorkflowOptions;
import java.util.List;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.myproject.cadence.cadence.config.CadenceExecutor;
import ru.myproject.cadence.cadence.config.CadenceProperties;
import ru.myproject.cadence.cadence.workflow.GetListElementsWorkflow;

@Service
@RequiredArgsConstructor
public class ListService {

    private final CadenceExecutor cadenceExecutor;
    private final CadenceProperties cadenceProperties;

    private WorkflowClient workflowClient;
    private WorkflowOptions workflowOptions;

    @PostConstruct
    public void init() {
        workflowClient = cadenceExecutor.getWorkflowClient(cadenceProperties);
        workflowOptions = cadenceExecutor.getWorkflowOptions(cadenceProperties.getCadenceOptions().getTaskList(),
                cadenceProperties.getCadenceOptions().getExecutionTimeout());
    }

    public void letsRock(List<String> strings) {

        GetListElementsWorkflow getListElementsWorkflow = workflowClient.newWorkflowStub(GetListElementsWorkflow.class, workflowOptions);

        cadenceExecutor.executeAsync(getListElementsWorkflow::getListElements, strings);
    }
}
