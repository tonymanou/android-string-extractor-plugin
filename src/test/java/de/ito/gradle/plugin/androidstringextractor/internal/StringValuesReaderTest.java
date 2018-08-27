package de.ito.gradle.plugin.androidstringextractor.internal;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;

import de.ito.gradle.plugin.androidstringextractor.internal.resource.StringRes;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StringValuesReaderTest {
  @Rule public TemporaryFolder folder = new TemporaryFolder();

  private StringValuesReader stringValuesReader;
  private XmlFileReader xmlFileReader;

  @Before public void setUp() throws Exception {
    xmlFileReader = mock(XmlFileReader.class);

    stringValuesReader = new StringValuesReader(xmlFileReader);
  }

  @Test public void when_readStringValuesFromFile_should_returnStringValues() throws Exception {
    String qualifier = "fr";
    StringValues expected = createDummyStringValues(qualifier);
    Document dummyStringValues = createDummyDocument();
    when(xmlFileReader.read(any(File.class))).thenReturn(dummyStringValues);
    File flavorPath = createFileStructure(qualifier);

    StringValues actual = stringValuesReader.read(flavorPath, qualifier);

    assertThat(actual, equalTo(expected));
  }

  @Test
  public void given_invalidFormatNode_when_readStringValuesFromFile_should_returnStringValues_andIgnoreInvalidNode()
          throws Exception {
    String qualifier = "fr";
    StringValues expected = createDummyStringValues(qualifier);
    Document dummyStringValues = createDummyDocumentWithInvalidNode();
    when(xmlFileReader.read(any(File.class))).thenReturn(dummyStringValues);
    File flavorPath = createFileStructure(qualifier);

    StringValues actual = stringValuesReader.read(flavorPath, qualifier);

    assertThat(actual, equalTo(expected));
  }



  static StringValues createDummyStringValues(String qualifier) {
    StringValues values = new StringValues(qualifier);

    values.getValues().add(new StringRes("name", "value"));

    return values;
  }

  static Document createDummyDocument() throws ParserConfigurationException {
    Document document = Util.createEmptyDocument();

    Element resources = document.createElement("resources");
    Element string = createStringNodeEntry(document);

    resources.appendChild(string);
    document.appendChild(resources);

    return document;
  }

  private static Element createStringNodeEntry(Document document) {
    Element string = document.createElement("string");
    Attr name = document.createAttribute("name");
    name.setValue("name");
    Text value = document.createTextNode("value");

    string.setAttributeNode(name);
    string.appendChild(value);
    return string;
  }

  private File createFileStructure(String qualifier) throws IOException {
    File flavorPath = new File(folder.getRoot(), "flavor/");
    String valueDirectory = qualifier == null || qualifier.isEmpty() ? "string" : "values-" + qualifier;
    File stringValuesFile = new File(flavorPath, "res/" + valueDirectory + "/string.xml");
    stringValuesFile.mkdirs();
    stringValuesFile.createNewFile();
    return flavorPath;
  }

  static Document createDummyDocumentWithInvalidNode() throws ParserConfigurationException {
    Document document = createDummyDocument();

    document.getElementsByTagName("resources").item(0).appendChild(createStringNodeEntryWithoutValue(document));

    return document;
  }

  private static Element createStringNodeEntryWithoutValue(Document document) {
    Element string = document.createElement("string");
    Attr name = document.createAttribute("name");
    name.setValue("withoutValue");

    string.setAttributeNode(name);
    return string;
  }
}