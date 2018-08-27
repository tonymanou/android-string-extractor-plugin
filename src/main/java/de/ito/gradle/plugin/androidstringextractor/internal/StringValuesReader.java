package de.ito.gradle.plugin.androidstringextractor.internal;

import java.io.*;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import de.ito.gradle.plugin.androidstringextractor.internal.resource.PluralRes;
import de.ito.gradle.plugin.androidstringextractor.internal.resource.StringArrayRes;
import de.ito.gradle.plugin.androidstringextractor.internal.resource.StringRes;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

class StringValuesReader {
  private XmlFileReader xmlFileReader;
  private Logger logger = Logger.getAnonymousLogger();

  StringValuesReader(XmlFileReader xmlFileReader) {
    this.xmlFileReader = xmlFileReader;
  }

  StringValues read(File flavorPath, String qualifier)
          throws ParserConfigurationException, SAXException, IOException {
    String valueDirectory = qualifier == null || qualifier.isEmpty() ? "values" : "values-" + qualifier;
    File stringValuesFile = new File(flavorPath, "res/" + valueDirectory + "/strings.xml");

    return resolveStringValues(stringValuesFile, qualifier);
  }

  private StringValues resolveStringValues(File stringValuesFile, String qualifier)
          throws ParserConfigurationException, SAXException, IOException {
    StringValues values = new StringValues(qualifier);
    if (!stringValuesFile.exists()) return values;

    Document document = xmlFileReader.read(stringValuesFile);
    NodeList nodes = document.getDocumentElement().getChildNodes();
    for (int i = 0, max = nodes.getLength(); i < max; i++) {
//      try {
      handleNode(nodes.item(i), values);
//      } catch (RuntimeException e) {
//        logNodeHandlingException(nodes.item(i), e);
//      }
    }
    return values;
  }

  private void handleNode(Node node, StringValues values) {
    String name = node.getNodeName();
    NamedNodeMap attributes = node.getAttributes();
    if (name == null || attributes == null) {
      return;
    }

    Node nameAttribute = attributes.getNamedItem("name");
    if (nameAttribute == null) {
      return;
    }
    String key = nameAttribute.getNodeValue();

    switch (name) {
      case "string":
        handleStringNode(node, key, attributes, values);
        break;
      case "plurals":
        handlePluralNode(node, key, values);
        break;
      case "string-array":
        handleStringArrayNode(node, key, values);
        break;
    }
  }

  private void handleStringNode(Node node, String key, NamedNodeMap attributes, StringValues values) {
    Node translatableAttribute = attributes.getNamedItem("translatable");
    if (translatableAttribute == null || !"false".equalsIgnoreCase(translatableAttribute.getNodeValue())) {
      values.put(new StringRes(key, convertNodeContentToText(node)));
    }
  }

  private void handlePluralNode(Node node, String key, StringValues values) {
    PluralRes pluralRes = new PluralRes(key);
    NodeList pluralNodes = node.getChildNodes();
    for (int i = 0, max = pluralNodes.getLength(); i < max; i++) {
      Node pluralNode = pluralNodes.item(i);
      String itemName = pluralNode.getNodeName();
      if ("item".equals(itemName)) {
        NamedNodeMap itemAttributes = pluralNode.getAttributes();
        if (itemAttributes == null) {
          continue;
        }
        Node quantityAttribute = itemAttributes.getNamedItem("quantity");
        if (quantityAttribute == null) {
          continue;
        }
        PluralRes.Quantity quantity = PluralRes.Quantity.valueOf(quantityAttribute.getNodeValue());
        pluralRes.add(quantity, convertNodeContentToText(pluralNode));
      }
    }
    values.put(pluralRes);
  }

  private void handleStringArrayNode(Node node, String key, StringValues values) {
    StringArrayRes arrayRes = new StringArrayRes(key);
    NodeList arrayNodes = node.getChildNodes();
    for (int i = 0, max = arrayNodes.getLength(); i < max; i++) {
      Node arrayNode = arrayNodes.item(i);
      String itemName = arrayNode.getNodeName();
      if ("item".equals(itemName)) {
        arrayRes.add(convertNodeContentToText(arrayNode));
      }
    }
    values.put(arrayRes);
  }

  private void logNodeHandlingException(Node node, RuntimeException e)  {
        try {
            logger.log(Level.SEVERE,"an unexpected error occurred while reading string_layouts.\n entry '"+convertNodeToText(node)+"' will not be considered");
        } catch (TransformerException e1) {
            logger.log(Level.SEVERE,"an unexpected error occurred while reading string_layouts.\n entry will not be considered");
        }
    }

    private String convertNodeToText(Node node )throws TransformerException {
        Transformer t = TransformerFactory.newInstance().newTransformer();
        t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        StringWriter sw = new StringWriter();
        t.transform(new DOMSource(node), new StreamResult(sw));
        return sw.toString();
    }

  private String convertNodeContentToText(Node node) {
    if (node == null) {
      return null;
    }

    NodeList nodes = node.getChildNodes();
    try (StringBuilderWriter writer = new StringBuilderWriter()) {
      Transformer t = TransformerFactory.newInstance().newTransformer();
      t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

      for (int i = 0, max = nodes.getLength(); i < max; i++) {
        Node child = nodes.item(i);
        if (child.getNodeType() == Node.COMMENT_NODE) {
          continue;
        }
        t.transform(new DOMSource(child), new StreamResult(writer));
      }

      String str = writer.toString();

      // Cleanup lines
      return Arrays.stream(str.split("\\s*\n\\s*"))
        .filter(s -> !s.trim().isEmpty())
        .collect(Collectors.joining(" "));
    } catch (TransformerException e) {
      throw new IllegalStateException("Failed to convert node to text", e);
    }
  }
}
