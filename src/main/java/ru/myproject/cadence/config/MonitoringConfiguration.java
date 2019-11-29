package ru.myproject.cadence.config;

import io.prometheus.client.exporter.MetricsServlet;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.Servlet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import ru.myproject.cadence.monitoring.MonitoringConfig;


@Configuration
@Slf4j
public class MonitoringConfiguration {

  @Bean
  public MonitoringConfig config(@Autowired ApplicationContext applicationContext,
      @Autowired Map<String, String> mandatoryLabels) {
    final MonitoringConfig config = new MonitoringConfig();
    String[] me = new String[6];
    me[0] = "^request_duration_.*$";
    me[1] = "^contract_*$";
    me[2] = "^claim_*$";
    me[3] = "^cancellation_*$";
    me[4] = "^payment_*$";
    me[5] = "^correction_*$";
    config.setMetricsEnable(me);
    config.setLabels(mandatoryLabels);
   // Map<String, CustomMetricsCollector> beans = applicationContext.getBeansOfType(CustomMetricsCollector.class);
//    beans.forEach((key, value) -> config.getCustomMetrics().addAll(value.initCustomMetrics()));
//    Map<String, CustomMetricsCollector> map = beans.entrySet().stream().collect(Collectors.toMap(
//        e -> e.getValue().getClass().getName(),
//        Entry::getValue
//    ));
  //  MonitoringAspect.setCustomizers(map);
    return config;
  }

  @Bean
  public Map<String, String> getMandatoryLabels(@Value("${spring.application}") String appName) {
    Map<String, String> map = new HashMap<>();
    map.put("component", appName);
    try {
      map.put("node", getIpAddress());
    } catch (SocketException e) {
      log.error(e.getMessage(), e);
      map.put("node", "127.0.0.1");
    }
    return map;
  }

  private String getIpAddress() throws SocketException {
    Enumeration e = NetworkInterface.getNetworkInterfaces();
    while (e.hasMoreElements()) {
      NetworkInterface n = (NetworkInterface) e.nextElement();
      Enumeration ee = n.getInetAddresses();
      while (ee.hasMoreElements()) {
        InetAddress i = (InetAddress) ee.nextElement();
        if (!i.isLoopbackAddress() && !(i instanceof Inet6Address)) {
          return i.getHostAddress();
        }
      }
    }
    return "127.0.0.1";
  }

  @Bean
  public TaskExecutor pool() {
    ThreadPoolTaskExecutor res = new ThreadPoolTaskExecutor();
    res.setCorePoolSize(2);
    res.setMaxPoolSize(2);
    return res;
  }

  @Bean
  public ServletRegistrationBean<Servlet> registerServlet(@Autowired MonitoringConfig config) {
    return new ServletRegistrationBean<>(new MetricsServlet(), config.getHttpUrl());
  }

}