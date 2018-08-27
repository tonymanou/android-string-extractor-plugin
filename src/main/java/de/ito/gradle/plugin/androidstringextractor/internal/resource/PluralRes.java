package de.ito.gradle.plugin.androidstringextractor.internal.resource;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.EnumMap;
import java.util.Map;

public class PluralRes implements Res {

  private final String key;
  private final Map<Quantity, String> values = new EnumMap<>(Quantity.class);

  public PluralRes(String key) {
    this.key = key;
  }

  public void add(Quantity quantity, String value) {
    values.put(quantity, value);
  }

  public Map<Quantity, String> getValues() {
    return values;
  }

  @Override
  public String getKey() {
    return key;
  }

  @Override public boolean equals(Object o) {
    return EqualsBuilder.reflectionEquals(this, o, false);
  }

  @Override public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this, false);
  }

  @Override public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  public enum Quantity {
    zero, one, two, few, many, other
  }
}
