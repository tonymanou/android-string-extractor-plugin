package de.ito.gradle.plugin.androidstringextractor.internal;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import de.ito.gradle.plugin.androidstringextractor.internal.resource.Res;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class StringValues {
  final String qualifier;
  private final Set<Res> values;
  private boolean hasChanged;

  StringValues(String qualifier) {
    this.qualifier = qualifier;
    this.values = new LinkedHashSet<>();
  }

  public Set<Res> getValues() {
    return values;
  }

  boolean put(Res value) {
    boolean notAlreadyAdded = values.add(value);
    hasChanged = true;
    return notAlreadyAdded;
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
