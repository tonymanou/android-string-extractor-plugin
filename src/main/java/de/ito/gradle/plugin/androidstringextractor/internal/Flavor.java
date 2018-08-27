package de.ito.gradle.plugin.androidstringextractor.internal;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.xml.sax.SAXException;

class Flavor {
  private final File path;
  private final StringValuesReader stringValuesReader;
  private final StringValuesWriter stringValuesWriter;
  private final LayoutScanner layoutScanner;
  private final ValuesQualifierScanner valuesQualifierScanner;

  Flavor(File path, StringValuesReader stringValuesReader,
      StringValuesWriter stringValuesWriter, LayoutScanner layoutScanner,
      ValuesQualifierScanner valuesQualifierScanner) {
    this.path = path;
    this.stringValuesReader = stringValuesReader;
    this.stringValuesWriter = stringValuesWriter;
    this.layoutScanner = layoutScanner;
    this.valuesQualifierScanner = valuesQualifierScanner;
  }

  String getName() {
    return path.getName();
  }

  StringValues readStringValues(String qualifier) throws IOException, SAXException, ParserConfigurationException {
    return stringValuesReader.read(path, qualifier);
  }

  void writeStringValues(StringValues stringValues)
      throws IOException, TransformerException, ParserConfigurationException {
    stringValuesWriter.write(stringValues, path);
  }

//  public void writeStringValues(Map<String, StringValues> stringValues) {
////    File stringValuesFile = new File(path, "strings.txt");
//    Map<String, String> defaultValues = stringValues.get(null).getValues();
//    List<String> qualifiers = stringValues.keySet().stream()
//            .filter(Objects::nonNull)
//            .collect(Collectors.toList());
//
//    for (Map.Entry<String, String> entry : defaultValues.entrySet()) {
//      String key = entry.getKey();
//      String others = qualifiers.stream()
//              .map(qualifier -> stringValues.get(qualifier).getValues().get(key))
//              .reduce(",", (base, n) -> base + "," + n);
//      System.err.println(key + "," + entry.getValue() + others);
//    }
////    stringValuesWriter.write(stringValues, path);
//  }

  List<String> readValuesQualifiers() {
    return valuesQualifierScanner.scan(path);
  }

  List<Layout> readLayouts() {
    return layoutScanner.scan(path);
  }

  @Override public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj, false);
  }

  @Override public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this, false);
  }

  @Override public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
}
