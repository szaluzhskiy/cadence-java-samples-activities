package ru.myproject.cadence.monitoring;


import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;


/**
 * Configuration for test REST controller
 *
 * @author Stan Fedetsov
 */
@Getter
@Setter
public class MonitoringConfig {
  // For jetty only!
  private int httpPort = 9090;
  private String httpUrl = "/metrics";
  private boolean jvmEnable = true;
  private int jvmScrapeInterval = 2000;
  private String[] metricsDisable;
  private String[] metricsEnable;
  private Map<String, String> labels = new HashMap<>();
  private Map<String, Double[]> buckets;
//  private List<MetricDef> customMetrics = new LinkedList<>();
}
