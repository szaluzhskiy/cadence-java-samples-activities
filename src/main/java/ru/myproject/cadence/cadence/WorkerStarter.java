package ru.myproject.cadence.cadence;

import com.uber.cadence.worker.Worker;
import com.uber.cadence.worker.WorkerOptions;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import ru.myproject.cadence.cadence.config.CadenceProperties;
import ru.myproject.cadence.cadence.workflow.GetListElementsWorkflow;

@Service
@RequiredArgsConstructor
public class WorkerStarter implements CommandLineRunner {

    private final CadenceProperties cadenceProperties;

    @Override
    public void run(String... args) throws Exception {

        Worker.Factory factory = new Worker.Factory(cadenceProperties.getHost(), cadenceProperties.getPort(),
                cadenceProperties.getDomain());

        Worker bsoWorker = factory.newWorker(cadenceProperties.getCadenceOptions().getTaskList(),
                getWorkerOptions(cadenceProperties.getCadenceOptions()));
        bsoWorker.registerWorkflowImplementationTypes(GetListElementsWorkflow.class);

        factory.start();
    }

    private WorkerOptions getWorkerOptions(CadenceProperties.CadenceOptions cadenceOptions) {
        return new WorkerOptions.Builder()
                .setMaxConcurrentActivityExecutionSize(cadenceOptions.getActivityPoolSize())
                .setMaxConcurrentWorkflowExecutionSize(cadenceOptions.getWorkflowPoolSize())
                .build();
    }
}
