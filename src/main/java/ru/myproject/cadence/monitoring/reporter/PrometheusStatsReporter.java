package ru.myproject.cadence.monitoring.reporter;

import static ru.myproject.cadence.monitoring.MetricsUtils.getMetricDefByName;

import com.uber.m3.tally.Buckets;
import com.uber.m3.tally.Capabilities;
import com.uber.m3.tally.CapableOf;
import com.uber.m3.tally.StatsReporter;
import com.uber.m3.util.Duration;
import java.util.Map;
import ru.myproject.cadence.monitoring.MetricDef;
import ru.myproject.cadence.monitoring.Metrics;

public class PrometheusStatsReporter implements StatsReporter {

  public PrometheusStatsReporter() {

  }

  @Override
  public Capabilities capabilities() {
    return CapableOf.REPORTING;
  }

  @Override
  public void flush() {
    System.out.println("Flush");
  }

  @Override
  public void close() {
    System.out.println("Closed");
  }

  @Override
  public void reportCounter(String name, Map<String, String> tags, long value) {
    String metricName = name.toLowerCase();
    metricName = metricName.replace("-", "_");
    System.out.format("CounterImpl %s: %f\n", metricName, value);
    Metrics.inc(getMetricDefByName(metricName));
  }

  @Override
  public void reportGauge(String name, Map<String, String> tags, double value) {
    System.out.format("GaugeImpl %s: %f\n", name, value);
  }

  @Override
  public void reportTimer(String name, Map<String, String> tags, Duration interval) {
    System.out.format("TimerImpl %s: %s %f\n", processMetricNameToPrometheusFormat(name), tags, interval.getSeconds());
    Metrics.observeCollector(Metrics.collectors.get(getMetricDefByName(processMetricNameToPrometheusFormat(name))), new String[]{"External", "cadence"}, interval.getSeconds());
  }

  @Override
  public void reportHistogramValueSamples(String name, Map<String, String> tags, Buckets buckets, double bucketLowerBound, double bucketUpperBound, long samples) {
    System.out.format("HistogramImpl bucket [%s] lower [%f] upper [%f] samples [%d]\n", name, bucketLowerBound, bucketUpperBound, samples);
  }

  @Override
  public void reportHistogramDurationSamples(String name, Map<String, String> tags, Buckets buckets, Duration bucketLowerBound, Duration bucketUpperBound, long samples) {
    System.out.format("HistogramImpl bucket [%s] lower [%s] upper [%s] samples [%d]\n", name, bucketLowerBound, bucketUpperBound, samples);
  }

  private String processMetricNameToPrometheusFormat(String name) {
    String metricName = name.toLowerCase();
    metricName = metricName.replace("-", "_");
    return metricName;
  }
}