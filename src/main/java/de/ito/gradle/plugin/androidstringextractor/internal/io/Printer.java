package de.ito.gradle.plugin.androidstringextractor.internal.io;

import java.io.IOException;

public interface Printer {

  void addHeaderRow(String[] columns);

  void addRow(String[] columns);

  void writeToDisk() throws IOException;
}
