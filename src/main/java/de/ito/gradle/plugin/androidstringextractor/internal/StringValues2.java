package de.ito.gradle.plugin.androidstringextractor.internal;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.LinkedHashMap;
import java.util.Map;

class StringValues2 {
  private final Map<String, StringValues> values;
  private boolean hasChanged;

  StringValues2() {
    values = new LinkedHashMap<>();
  }

  StringValues getValues() {
    return values.get(null);
  }

  void put(String key, StringValues value) {
    values.put(key, value);
    hasChanged = true;
  }

  public boolean hasChanged() {
    return hasChanged;
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
}
