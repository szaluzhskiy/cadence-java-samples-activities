package ru.myproject.cadence.monitoring;

import com.uber.cadence.internal.metrics.MetricsType;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;
import io.prometheus.client.Histogram.Timer;
import io.prometheus.client.SimpleCollector;
import io.prometheus.client.Summary;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;


/**
 * Definition of scraped metrics
 */
@Slf4j
public class Metrics<METRIC_DEFINITION_STRATEGY> {

  private static final Pattern GET_MODULE_NAME_JAR = Pattern.compile("^file:.+[/|\\\\]([a-zA-Z0-9-.]+).jar!.+$");
  private static final Pattern GET_MODULE_NAME_CLASS = Pattern.compile("^.+[/|\\\\]([a-zA-Z0-9]+)\\.class$");

  protected static final String[] MANDATOTY_LABELS = {"component", "node"};


  public final static Map<MetricDef, SimpleCollector> collectors = new HashMap<>();
  protected static String[] labelNames;
  protected static String[] labelValues;

  // Initialize metrics
  static {
    reset();
  }

  static Map<String, Function<Field, MetricDef>>
      METRIC_DEFINITION_STRATEGY = new HashMap();


  public static MetricDef buildCounterMetricDefinition(Field field) {
    String metricName = null;
    try {
      metricName = (String) MetricsType.class.getField(field.getName()).get(null);
      metricName = metricName.toLowerCase();
      metricName = metricName.replace("-", "_");
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (NoSuchFieldException e) {
      e.printStackTrace();
    }
    return new MetricDef(metricName, field.getName(), Counter.class, null);
  }

  public static MetricDef buildLatencyMetricDefinition(Field field) {
    //TODO: Fix bucket = null
    String metricName = null;
    try {
      metricName = (String) MetricsType.class.getField(field.getName()).get(null);
      metricName = metricName.toLowerCase();
      metricName = metricName.replace("-", "_");
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (NoSuchFieldException e) {
      e.printStackTrace();
    }
    return new MetricDef(metricName, field.getName(), Histogram.class, null);
  }

  /**
   * Method initializes collectors from configuration
   */
  public static void init(MonitoringConfig config) {
    METRIC_DEFINITION_STRATEGY.put("COUNTER", Metrics::buildCounterMetricDefinition);
    METRIC_DEFINITION_STRATEGY.put("LATENCY", Metrics::buildLatencyMetricDefinition);
    processMandatoryLabelValues(extractMandatoryValues(config));
    processOptionalLabels(extractOptionalLabels(config));

    Field[] mtFields = MetricsType.class.getFields();
    for (Field mtField : mtFields) {
      MetricDef md = null;
      if (mtField.getName().contains("COUNTER")) {
        md = METRIC_DEFINITION_STRATEGY.get("COUNTER")
            .apply(mtField);
        registerCollector(md);
      } else if (mtField.getName().contains("LATENCY")) {
        md = METRIC_DEFINITION_STRATEGY.get("LATENCY")
            .apply(mtField);
        registerCollector(md);
      }
    }
    //CollectorRegistry.defaultRegistry.register(collectors);
  }

  /**
   * Complete reset of the metrics
   */
  public static void reset() {
    if (MetricsUtils.isInitialized()) {
      for (SimpleCollector sc : collectors.values()) {
        CollectorRegistry.defaultRegistry.unregister(sc);
      }
    }
    collectors.clear();
    labelNames = MANDATOTY_LABELS;
    labelValues = null;
  }


  public static SimpleCollector get(MetricDef md) {
    return collectors.get(md);
  }

  /**
   * Obtains the collector by its name
   */
  public static SimpleCollector get(String metricName) {
    if (Objects.nonNull(metricName)) {
      return MetricsUtils.getCollectorByName(metricName);
    } else {
      return null;
    }
  }

  /**
   * Increment the collector
   */
  public static void inc(MetricDef md, String... optionalLabels) {
    String[] labels = MetricsUtils.mergeLabelValues(optionalLabels);
    if (Objects.nonNull(collectors.get(md))) {
      MetricsUtils.incCollector(collectors.get(md), labels);
    }
  }

  /**
   * Increments the collector identified by its name
   */
  public static void inc(String metricName, String... optionalLabels) {
    inc(MetricsUtils.getMetricDefByName(metricName), optionalLabels);
  }

  /**
   * Decrement the collector
   */
  public static void dec(MetricDef md, String... optionalLabels) {
    String[] labels = MetricsUtils.mergeLabelValues(optionalLabels);
    MetricsUtils.decCollector(collectors.get(md), labels);
  }

  /**
   * Sets the double values of the collector
   */
  public static void set(MetricDef md, double value, String... optionalLabels) {
    String[] labels = MetricsUtils.mergeLabelValues(optionalLabels);
    MetricsUtils.setCollector(collectors.get(md), labels, value);
  }

  /**
   * Sets the long value of the collector
   */
  public static void set(MetricDef md, long value, String... optionalLabels) {
    set(md, (double) value, optionalLabels);
  }

  /**
   * Starts the timer
   */
  public static Timer startTimer(MetricDef md, String... optionalLabels) {
    if (md == null) {
      return null;
    }
    String[] labels = MetricsUtils.mergeLabelValues(optionalLabels);
    SimpleCollector c = collectors.get(md);
    if (Objects.nonNull(c) && Objects.equals(c.getClass(), (Histogram.class))) {
      return ((Histogram) c).labels(labels).startTimer();
    } else {
      return null;
    }
  }

  /**
   * Starts the timer on collector identified by its name
   */
  public static Timer startTimer(String metricName, String... optionalLabels) {
    return startTimer(MetricsUtils.getMetricDefByName(metricName), optionalLabels);
  }

//  /**
//   * Obtains either a name of executed JAR or a class of executed main() function
//   */
//  public static String getModuleName() {
//    StackTraceElement[] elements = Thread.currentThread().getStackTrace();
//    StackTraceElement main = elements[elements.length - 1];
//    String jar = Metrics.class.getResource("/" + main.getClassName().replace('.', '/') + ".class").getPath();
//    Matcher m = GET_MODULE_NAME_JAR.matcher(jar);
//    if (m.matches()) {
//      return m.group(1);
//    }
//    m = GET_MODULE_NAME_CLASS.matcher(jar);
//    if ((m.matches())) {
//      return m.group(1);
//    }
//    return "UNKNOWN";
//  }


  /* (non-Javadoc)
   * Returns a list of default metrics
   */
  private static List<MetricDef> processDefaultCollectors(MonitoringConfig config) {
    List<MetricDef> res = new ArrayList<>();
    Field[] fields = Metrics.class.getDeclaredFields();
    for (Field f : fields) {
      if (Modifier.isStatic(f.getModifiers()) && f.getType().equals(MetricDef.class)) {
        MetricDef md = processCollector(f, config);
        if (md != null && !res.contains(md)) {
          res.add(md);
        }
      }
    }
    return res;
  }

  /*  *//* (non-Javadoc)
   * Appends custom metrics from configuration
   *//*
  private static List<MetricDef> processCustomMetrics(List<MetricDef> metrics, MonitoringConfig config) {
    List<MetricDef> processCustomMetrics = new ArrayList<>();
    if (config != null && config.getCustomMetrics() != null) {
      processCustomMetrics = getProcessCustomMetrics(metrics, config);
    }
    return processCustomMetrics;
  }

  private static List<MetricDef> getProcessCustomMetrics(List<MetricDef> metrics, MonitoringConfig config) {
    for (MetricDef md : config.getCustomMetrics()) {
      MetricDef cfg = processCollector(md, config);
      if (md != null && !metrics.contains(cfg)) {
        metrics.add(cfg);
      }
    }
    return metrics;
  }*/


  private static boolean findMatch(String s, String... re) {
    if (re != null) {
      for (String exp : re) {
        if (s.matches(exp)) {
          return true;
        }
      }
    }
    return false;
  }

  /* (non-Javadoc)
   * Extracts mandatory label values from configuration. It's either getting the
   * values from config or leaves them empty or null if not provided
   */
  private static String[] extractMandatoryValues(MonitoringConfig config) {
    String[] res = new String[MANDATOTY_LABELS.length];
    if (config != null && config.getLabels() != null && !config.getLabels().isEmpty()) {
      for (int i = 0; i < MANDATOTY_LABELS.length; i++) {
        res[i] = config.getLabels().get(MANDATOTY_LABELS[i]);
      }
    }
    return res;
  }

  /* (non-Javadoc)
   * Builds a map of optional labels (name+value) making sure nulls are replaced with
   * an empty strings
   */
  private static Map<String, String> extractOptionalLabels(MonitoringConfig config) {
    if (config != null && config.getLabels() != null) {
      return getExtractOptionalLabels(config);
    }
    return null;
  }

  private static Map<String, String> getExtractOptionalLabels(MonitoringConfig config) {
    Map<String, String> res = new HashMap<>();
    List<String> mandatory = Arrays.asList(MANDATOTY_LABELS);
    for (Map.Entry<String, String> e : config.getLabels().entrySet()) {
      if (!mandatory.contains(e.getKey())) {
        if (Objects.nonNull(e.getValue())) {
          res.put(e.getKey(), e.getValue());
        } else {
          res.put(e.getKey(), "");
        }
      }
    }
    return res;
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

  public static void observeCollector(SimpleCollector collector, String[] labels, Double amt) {
    if (Objects.nonNull(collector) && collector instanceof Histogram) {
      ((Histogram) collector).labels(labels).observe(amt);
    }
  }

  /*  *//* (non-Javadoc)
   * Sets mandatory label values to specified ones, defaults to:
   *  - component: main class
   *  - node: local IP address
   */
  private static void processMandatoryLabelValues(String... values) {
    List<String> res = new ArrayList<>();
    if (biggerThan(values, 1) && !MetricsUtils.isStringEmpty(values[0])) {
      res.add(values[0]);
    } else {
      res.add(getModuleName());
    }
    if (biggerThan(values, 2) && !MetricsUtils.isStringEmpty(values[1])) {
      res.add(values[1]);
    } else {
      res.add("127.0.0.1");
    }
    labelValues = res.toArray(new String[0]);
  }

  private static boolean biggerThan(String[] array, int size) {
    return (array != null && array.length >= size);
  }

  /**
   * Obtains either a name of executed JAR or a class of executed main() function
   */
  public static String getModuleName() {
    StackTraceElement[] elements = Thread.currentThread().getStackTrace();
    StackTraceElement main = elements[elements.length - 1];
    String jar = Metrics.class.getResource("/" + main.getClassName().replace('.', '/') + ".class").getPath();
    Matcher m = GET_MODULE_NAME_JAR.matcher(jar);
    if (m.matches()) {
      return m.group(1);
    }
    m = GET_MODULE_NAME_CLASS.matcher(jar);
    if ((m.matches())) {
      return m.group(1);
    }
    return "UNKNOWN";
  }


  /* (non-Javadoc)
   * Appends optional label names and values
   */
  private static void processOptionalLabels(Map<String, String> opts) {
    if (opts != null && opts.size() > 0) {
      String[] newNames = new String[MANDATOTY_LABELS.length + opts.size()];
      System.arraycopy(labelNames, 0, newNames, 0, labelNames.length);
      String[] newValues = new String[MANDATOTY_LABELS.length + opts.size()];
      System.arraycopy(labelValues, 0, newValues, 0, labelValues.length);
      labelNames = newNames;
      labelValues = newValues;

      List<Map.Entry<String, String>> entries = new ArrayList<>(opts.entrySet());
      for (int i = 0; i < entries.size(); i++) {
        labelNames[MANDATOTY_LABELS.length + i] = entries.get(i).getKey();
        labelValues[MANDATOTY_LABELS.length + i] = entries.get(i).getValue();
      }
    }
  }

  private static MetricDef processCollector(Field field, MonitoringConfig config) {
    try {
      return processCollector((MetricDef) field.get(null), config);
    } catch (IllegalAccessException e) {
      log.error("Error in metrics initialization.", e);
    }
    return null;
  }

  private static MetricDef processCollector(MetricDef md, MonitoringConfig config) {
    if (md != null) {
      Double[] buckets;
      if (Objects.nonNull(config) && Objects.nonNull(config.getBuckets())
          && Objects.nonNull(config.getBuckets().get(md.getName()))) {
        buckets = config.getBuckets().get(md.getName());
      } else {
        buckets = md.getBuckets();
      }
      return new MetricDef(md.getName(), md.getHelp(), md.getType(), buckets);
    }
    return null;
  }

  private static void registerCollector(MetricDef md) {
    if (md.getType().equals(Counter.class)) {
      collectors.put(md, registerCounter(md.getName(), md.getHelp()));
    } else if (md.getType().equals(Histogram.class)) {
      collectors.put(md, registerHistogram(md.getName(), md.getHelp(), md.getBuckets()));
    } else if (md.getType().equals(Summary.class)) {
      collectors.put(md, registerSummary(md.getName(), md.getHelp()));
    } else {
      collectors.put(md, registerGauge(md.getName(), md.getHelp()));
    }
  }

  private static SimpleCollector registerCounter(String name, String help) {
    return Counter.build(name, help).labelNames(labelNames).register();
  }

  private static SimpleCollector registerHistogram(String name, String help, Double... cfgBuckets) {
    double[] buckets;
    if (Objects.nonNull(cfgBuckets)) {
      buckets = Stream.of(cfgBuckets).mapToDouble(Double::doubleValue).toArray();
    } else {
      buckets = null;
    }
    Histogram.Builder builder = Histogram.build(name, help).labelNames(labelNames);
    if (buckets != null) {
      builder.buckets(buckets);
    }
    return builder.register();
  }

  private static SimpleCollector registerSummary(String name, String help) {
    Summary.Builder builder = Summary.build(name, help).labelNames(labelNames);
    return builder.register();
  }

  private static SimpleCollector registerGauge(String name, String help) {
    return Gauge.build(name, help).labelNames(labelNames).register();
  }
}
