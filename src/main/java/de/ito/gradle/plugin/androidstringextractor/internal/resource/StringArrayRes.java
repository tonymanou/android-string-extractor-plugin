package de.ito.gradle.plugin.androidstringextractor.internal.resource;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.LinkedList;
import java.util.List;

public class StringArrayRes implements Res {

  private final String key;
  public final List<String> values = new LinkedList<>();

  public StringArrayRes(String key) {
    this.key = key;
  }

  public void add(String value) {
    values.add(value);
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
}
