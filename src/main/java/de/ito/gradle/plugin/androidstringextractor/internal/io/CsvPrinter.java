package de.ito.gradle.plugin.androidstringextractor.internal.io;

import org.apache.commons.csv.CSVPrinter;

import java.io.OutputStream;

public class CsvPrinter implements Printer {

  private final CSVPrinter printer;

  public CsvPrinter(OutputStream out) {
    printer = new CSVPrinter(out);
  }

  @Override
  public void addHeaderRow(String[] columns) {
    printer.println(columns);
  }

  @Override
  public void addRow(String[] columns) {
    printer.println(columns);
  }

  @Override
  public void writeToDisk() {
    // Nothing to do
  }
}
