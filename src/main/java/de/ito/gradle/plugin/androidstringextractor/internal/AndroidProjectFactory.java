package de.ito.gradle.plugin.androidstringextractor.internal;

import java.io.File;

public class AndroidProjectFactory {
  LayoutParser layoutParser;
  XmlFileReader xmlFileReader;
  XmlFileWriter xmlFileWriter;
  StringValuesReader stringValuesReader;
  StringValuesWriter stringValuesWriter;
  LayoutScanner layoutScanner;
  FlavorScanner flavorScanner;
  ReferenceReplacer referenceReplacer;
  ValuesQualifierScanner valuesQualifierScanner;

  public AndroidProjectFactory() {
    layoutParser = new LayoutParser();
    xmlFileReader = new XmlFileReader();
    xmlFileWriter = new XmlFileWriter();
    referenceReplacer = new ReferenceReplacer();
    stringValuesReader = new StringValuesReader(xmlFileReader);
    stringValuesWriter = new StringValuesWriter(xmlFileWriter);
    layoutScanner =
        new LayoutScanner(xmlFileReader, layoutParser, xmlFileWriter, referenceReplacer);
    valuesQualifierScanner = new ValuesQualifierScanner();
    flavorScanner = new FlavorScanner(stringValuesReader, stringValuesWriter, layoutScanner, valuesQualifierScanner);
  }

  AndroidProject create(File projectPath) {
    return new AndroidProject(projectPath, flavorScanner);
  }
}
