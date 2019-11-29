package ru.myproject.cadence.monitoring;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;
import io.prometheus.client.SimpleCollector;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MetricsUtils {

  private MetricsUtils() {

  }

  public static int size() {
    return Metrics.collectors.size();
  }

  private static String[] cloneArray(String... array) {
    String[] res = new String[array.length];
    System.arraycopy(array, 0, res, 0, array.length);
    return res;
  }

  public static SimpleCollector getCollectorByName(String name) {
    if (Objects.nonNull(Metrics.collectors) && !isStringEmpty(name)) {
      for (Map.Entry<MetricDef, SimpleCollector> c : Metrics.collectors.entrySet()) {
        if (name.equals(c.getKey().getName())) {
          return c.getValue();
        }
      }
    }
    return null;
  }

  public static boolean isStringEmpty(String str) {
    return (str == null || str.trim().isEmpty());
  }

  public static MetricDef getMetricDefByName(String name) {
    if (Metrics.collectors != null && !isStringEmpty(name)) {
      for (Map.Entry<MetricDef, SimpleCollector> c : Metrics.collectors.entrySet()) {
        if (name.equals(c.getKey().getName())) {
          return c.getKey();
        }
      }
    }
    return null;
  }

  public static String[] mandatoryLabelNames() {
    return cloneArray(Metrics.MANDATOTY_LABELS);
  }

  /**
   * Returns the clone of array of the label names
   */
  public static String[] getLabelNames() {
    return cloneArray(Metrics.labelNames);
  }

  /**
   * Returns the clone of array of the label values
   */
  public static String[] getLabelValues() {
    return cloneArray(Metrics.labelValues);
  }

  /**
   * Obtains long value of the gauge
   */
  public static Long getLong(MetricDef md, String... optionalLabels) {
    Double res = getDouble(md, optionalLabels);
    if (Objects.nonNull(res)) {
      return res.longValue();
    } else {
      return null;
    }
  }

  /**
   * Obtains long value of the gauge identified by its name
   */
  public static Long getLong(String metricName, String... optionalLabels) {
    Double res = getDouble(metricName, optionalLabels);
    if (Objects.nonNull(res)) {
      return res.longValue();
    } else {
      return null;
    }
  }

  public static Double getDouble(MetricDef md, String... optionalLabels) {
    String[] labels = mergeLabelValues(optionalLabels);
    SimpleCollector res = Metrics.collectors.get(md);
    if (res instanceof Gauge) {
      return ((Gauge) res).labels(labels).get();
    }
    return null;
  }

  /**
   * Obtains double value of the gauge identified by its name
   */
  public static Double getDouble(String metricName, String... optionalLabels) {
    MetricDef md = getMetricDefByName(metricName);
    if (Objects.nonNull(md)) {
      return getDouble(md, optionalLabels);
    } else {
      return null;
    }
  }

  public static String[] mergeLabelValues(String... optionals) {
    String[] res = new String[Metrics.labelValues.length];
    System.arraycopy(Metrics.labelValues, 0, res, 0, Metrics.labelValues.length);
    if (optionals != null) {
      for (int i = 0, j = (Metrics.MANDATOTY_LABELS.length + i); (i < optionals.length && j < res.length); i++, j++) {
        res[j] = optionals[i];
      }
    }
    return res;
  }

  /**
   * Checks if the metrics have been initialized
   */
  public static boolean isInitialized() {
    return (Metrics.collectors != null && Metrics.collectors.size() > 0);
  }



  /**
   * Sets histogram with a value
   */
  public static void observe(MetricDef md, double amt, String... optionalLabels) {
    String[] labels = MetricsUtils.mergeLabelValues(optionalLabels);
    observeCollector(Metrics.collectors.get(md), labels, amt);
  }

  /**
   * Sets histogram identified by its name with a value
   */
  public static void observe(String metricName, double amt, String... optionalLabels) {
    observe(MetricsUtils.getMetricDefByName(metricName), amt, optionalLabels);
  }

  private static void observeCollector(SimpleCollector collector, String[] labels, Double amt) {
    if (Objects.nonNull(collector) && collector instanceof Histogram) {
      ((Histogram) collector).labels(labels).observe(amt);
    }
  }


  public static void incCollector(SimpleCollector collector, String... labels) {
    if (collector instanceof Gauge) {
      ((Gauge) collector).labels(labels).inc();
    } else {
      ((Counter) collector).labels(labels).inc();
    }
  }

  public static void decCollector(SimpleCollector collector, String... labels) {
    if (Objects.nonNull(collector) && collector instanceof Gauge) {
      ((Gauge) collector).labels(labels).dec();
    }
  }

  public static void setCollector(SimpleCollector collector, String[] labels, Double value) {
    if (Objects.nonNull(collector) && collector instanceof Gauge) {
      ((Gauge) collector).labels(labels).set(value);
    }
  }


  public static void peakCollector(SimpleCollector collector, String[] labels, Double value) {
    if (Objects.nonNull(collector) && collector instanceof Gauge) {
      double peak = ((Gauge) collector).labels(labels).get();
      if (value != null && value > peak) {
        ((Gauge) collector).labels(labels).set(value);
      }
    }
  }

  /**
   * Updates a peak value
   */
  public static void peak(MetricDef md, double value, String... optionalLabels) {
    String[] labels = MetricsUtils.mergeLabelValues(optionalLabels);
    MetricsUtils.peakCollector(Metrics.collectors.get(md), labels, value);
  }

}
