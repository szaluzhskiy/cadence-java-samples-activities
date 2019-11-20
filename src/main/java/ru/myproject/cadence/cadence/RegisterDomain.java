package ru.myproject.cadence.cadence;

import com.uber.cadence.DomainAlreadyExistsError;
import com.uber.cadence.RegisterDomainRequest;
import com.uber.cadence.serviceclient.IWorkflowService;
import com.uber.cadence.serviceclient.WorkflowServiceTChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import ru.myproject.cadence.cadence.config.CadenceProperties;

@Service
@Slf4j
@RequiredArgsConstructor
public class RegisterDomain implements CommandLineRunner {

    private final CadenceProperties cadenceProperties;

    @Override
    public void run(String... args) throws Exception {

        log.info("trying to register domain :{} using host:{} and port:{}", cadenceProperties.getDomain(),
                cadenceProperties.getHost(), cadenceProperties.getPort());

        IWorkflowService cadenceService = new WorkflowServiceTChannel(
                cadenceProperties.getHost(), cadenceProperties.getPort());
        RegisterDomainRequest request = new RegisterDomainRequest();
        request.setDescription("simple domain");
        request.setEmitMetric(false);
        request.setName(cadenceProperties.getDomain());
        int retentionPeriodInDays = 5;
        request.setWorkflowExecutionRetentionPeriodInDays(retentionPeriodInDays);
        try {
            cadenceService.RegisterDomain(request);
            log.info("Successfully registered domain {} with retentionDays={}", cadenceProperties.getDomain(),
                    retentionPeriodInDays);
        } catch (DomainAlreadyExistsError e) {
            log.error("Domain already exists {} {}", cadenceProperties.getDomain(), e);
        }
    }
}
