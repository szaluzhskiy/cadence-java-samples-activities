package ru.myproject.cadence.cadence.config;

import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.myproject.cadence.cadence.workflow.GetListElementsWorkflow;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Configuration
@ConfigurationProperties("app.cadence")
public class CadenceProperties {

    @NotBlank
    private String domain;
    @NotBlank
    private String host;
    @NotBlank
    private Integer port;

    @Bean
    @ConfigurationProperties("app.cadence.options")
    public CadenceOptions<GetListElementsWorkflow> getCadenceOptions() {
        return new CadenceOptions<>();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CadenceOptions<E> {

        private String taskList;
        private Integer executionTimeout;
        private Integer workflowPoolSize;
        private Integer activityPoolSize;
    }
}
