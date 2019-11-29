package ru.myproject.cadence.config;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class BeanProvider implements ApplicationContextAware {

  private static ApplicationContext context;

  static void setContext(ApplicationContext ac) {
    context = ac;
  }

  public static MeterRegistry meterRegistry() {
    return context.getBean(MeterRegistry.class);
  }

  @Override
  public void setApplicationContext(ApplicationContext ac) {
    context = ac;
  }
}
