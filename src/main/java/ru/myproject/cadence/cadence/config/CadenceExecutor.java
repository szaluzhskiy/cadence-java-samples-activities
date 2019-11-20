package ru.myproject.cadence.cadence.config;

import com.uber.cadence.client.WorkflowClient;
import com.uber.cadence.client.WorkflowClientOptions;
import com.uber.cadence.client.WorkflowOptions;
import com.uber.cadence.workflow.ChildWorkflowOptions;
import com.uber.cadence.workflow.Functions;
import com.uber.cadence.workflow.Workflow;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

@Service
public class CadenceExecutor {

    public WorkflowClient getWorkflowClient(String cadenceHost, int cadencePort, String cadenceDomain) {
        WorkflowClientOptions options = new WorkflowClientOptions.Builder().build();
        return WorkflowClient.newInstance(cadenceHost, cadencePort, cadenceDomain, options);
    }

    public WorkflowClient getWorkflowClient(CadenceProperties cadenceProperties) {
        return getWorkflowClient(cadenceProperties.getHost(), cadenceProperties.getPort(), cadenceProperties.getDomain());
    }

    public <T> T newChildWorkflowStub(Class<T> workflowInterface, ChildWorkflowOptions options) {
        return Workflow.newChildWorkflowStub(workflowInterface, options);
    }

    public <A1, R> CompletableFuture<R> executeAsync(Functions.Func1<A1, R> workflow, A1 arg1) {
        return  WorkflowClient.execute(workflow, arg1);
    }

    public WorkflowOptions getWorkflowOptions(String taskList, Integer timeout) {
        return new WorkflowOptions.Builder()
                .setTaskList(taskList)
                .setExecutionStartToCloseTimeout(Duration
                        .ofSeconds(timeout))
                .build();
    }

    public <A1> CompletableFuture<Void> executeAsync(Functions.Proc1<A1> workflow, A1 arg1) {
        return WorkflowClient.execute(workflow, arg1);
    }
}
