package de.ito.gradle.plugin.androidstringextractor.internal;

import java.io.Serializable;
import java.io.Writer;

public class StringBuilderWriter extends Writer implements Serializable {
  private final StringBuilder builder;

  public StringBuilderWriter() {
    builder = new StringBuilder();
  }

  public Writer append(char value) {
    builder.append(value);
    return this;
  }

  public Writer append(CharSequence value) {
    builder.append(value);
    return this;
  }

  public Writer append(CharSequence value, int start, int end) {
    builder.append(value, start, end);
    return this;
  }

  public void close() {
  }

  public void flush() {
  }

  public void write(String value) {
    if (value != null) {
      builder.append(value);
    }
  }

  public void write(char[] value, int offset, int length) {
    if (value != null) {
      builder.append(value, offset, length);
    }
  }

  public String toString() {
    return builder.toString();
  }
}
