package ru.myproject.cadence.monitoring;

import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Metric definition
 *
 * @author Stan Fedetsov
 */
@Getter
@AllArgsConstructor
@Builder
public class MetricDef {

  private String name;
  private String help;
  private Class<?> type;
  private Double[] buckets;

  @Override
  public boolean equals(Object o) {
    if (o == null || !o.getClass().equals(MetricDef.class)) {
      return false;
    }
    return this.hashCode() == o.hashCode();
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, help, type);
  }
}
