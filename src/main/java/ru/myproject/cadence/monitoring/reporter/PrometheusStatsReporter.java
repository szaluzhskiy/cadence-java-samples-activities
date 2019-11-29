package ru.myproject.cadence.monitoring.reporter;

import com.uber.m3.tally.Buckets;
import com.uber.m3.tally.Capabilities;
import com.uber.m3.tally.CapableOf;
import com.uber.m3.tally.StatsReporter;
import com.uber.m3.util.Duration;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.distribution.Histogram;
import java.util.Map;
import ru.myproject.cadence.config.BeanProvider;

public class PrometheusStatsReporter implements StatsReporter {

  private MeterRegistry registry;
  private Counter cadenceDecisionScheduledToStartLatency;

  public PrometheusStatsReporter() {
    this.registry = BeanProvider.meterRegistry();
    init();
  }

  private void init() {
    cadenceDecisionScheduledToStartLatency = Counter.builder("cadenceDecisionScheduledToStartLatency")
        .description("some description")
        .register(registry);

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
    System.out.format("reportCounter %s: %s: %f\n", name, tags, value);
  }

  @Override
  public void reportGauge(String name, Map<String, String> tags, double value) {
    System.out.format("GaugeImpl %s: %f\n", name, value);
  }

  @Override
  public void reportTimer(String name, Map<String, String> tags, Duration interval) {
    System.out.format("TimerImpl %s: %s\n", name, interval);
    if("cadence-decision-scheduled-to-start-latency".equals(name)) {
      cadenceDecisionScheduledToStartLatency.increment(interval.getSeconds());
    }
  }

  @Override
  public void reportHistogramValueSamples(String name, Map<String, String> tags, Buckets buckets,
      double bucketLowerBound, double bucketUpperBound, long samples) {
    System.out.format("HistogramImpl bucket [%s] lower [%f] upper [%f] samples [%d]\n", name,
        bucketLowerBound, bucketUpperBound, samples);
  }

  @Override
  public void reportHistogramDurationSamples(String name, Map<String, String> tags, Buckets buckets,
      Duration bucketLowerBound, Duration bucketUpperBound, long samples) {
    System.out.format("HistogramImpl bucket [%s] lower [%s] upper [%s] samples [%d]\n", name,
        bucketLowerBound, bucketUpperBound, samples);
  }
}