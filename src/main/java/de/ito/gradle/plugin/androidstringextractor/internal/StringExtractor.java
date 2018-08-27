package de.ito.gradle.plugin.androidstringextractor.internal;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

public class StringExtractor {
  private final AndroidProjectFactory factory;

  public StringExtractor(AndroidProjectFactory factory) {
    this.factory = factory;
  }

  public Map<String, Map<String, StringValues>> extract(String projectPath)
      throws IOException, ParserConfigurationException, SAXException {
    AndroidProject project = factory.create(new File(projectPath));
    List<Flavor> flavors = project.readFlavors();
    Map<String, Map<String, StringValues>> f = new HashMap<>();
    for (Flavor flavor : flavors) {
      f.put(flavor.getName(), handleFlavor(flavor));
    }
    return f;
  }

  private Map<String, StringValues> handleFlavor(Flavor flavor)
      throws IOException, ParserConfigurationException, SAXException {
    Map<String, StringValues> stringValues = new HashMap<>();
    StringValues defaultStringValues = flavor.readStringValues(null);

    stringValues.put(null, defaultStringValues);

    List<String> qualifiers = flavor.readValuesQualifiers();
    for (String qualifier : qualifiers) {
      StringValues qualifiedStringValues = flavor.readStringValues(qualifier);
      stringValues.put(qualifier, qualifiedStringValues);
    }

//    if(stringValues.hasChanged()) flavor.writeStringValues(stringValues);
    // TODO write CSV file
    return stringValues;
//    flavor.writeStringValues(stringValues);
  }

//  private void handleLayout(Layout layout, StringValues stringValues)
//      throws TransformerException, IOException, ParserConfigurationException, SAXException,
//      XPathExpressionException {
//    List<StringOccurrence> stringOccurrences = layout.readStrings();
//    for (StringOccurrence string : stringOccurrences) {
//      handleString(string, layout, stringValues);
//    }
//
//    layout.writeStrings(stringOccurrences);
//  }
//
//  private void handleString(StringOccurrence occurrence, Layout layout, StringValues stringValues) {
//    if (occurrence.hasHardCodedValue()) {
//      String key = layout.computeStringReference(occurrence);
//      stringValues.put(key, occurrence.getValue());
//      occurrence.replaceHardCodedValueByReference(key);
//    }
//  }
}
