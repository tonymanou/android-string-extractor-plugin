package de.ito.gradle.plugin.androidstringextractor.internal;

import java.io.File;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FlavorTest {
  private StringValuesReader stringValuesReader;
  private StringValuesWriter stringValuesWriter;
  private LayoutScanner layoutScanner;
  private ValuesQualifierScanner valuesQualifierScanner;
  private File path;

  private Flavor flavor;

  @Before public void setUp() throws Exception {
    stringValuesReader = mock(StringValuesReader.class);
    stringValuesWriter = mock(StringValuesWriter.class);
    layoutScanner = mock(LayoutScanner.class);
    valuesQualifierScanner = mock(ValuesQualifierScanner.class);
    path = mock(File.class);

    flavor = new Flavor(path, stringValuesReader, stringValuesWriter, layoutScanner, valuesQualifierScanner);
  }

  @Test public void when_readStringValues_should_returnStringValues() throws Exception {
    String qualifier = "fr";
    StringValues expected = dummyStringValues(qualifier);
    when(stringValuesReader.read(any(File.class), eq(qualifier))).thenReturn(expected);

    StringValues actual =
        flavor.readStringValues(qualifier);

    assertThat(actual, equalTo(expected));
  }

  @Test public void when_writeStringValues_should_writeStringValues() throws Exception {
    String qualifier = "fr";
    StringValues stringValues = dummyStringValues(qualifier);

    flavor.writeStringValues(stringValues);

    verify(stringValuesWriter).write(eq(stringValues), any(File.class));
  }

  @Test public void when_readLayouts_should_returnLayouts() throws Exception {
    List<Layout> expected = dummyLayouts();
    when(layoutScanner.scan(any(File.class))).thenReturn(expected);

    List<Layout> actual = flavor.readLayouts();

    assertThat(actual, equalTo(expected));
  }

  private StringValues dummyStringValues(String qualifier) {
    StringValues stringValues = new StringValues(qualifier);

    return stringValues;
  }

  private List<Layout> dummyLayouts() {
    Layout layout = new Layout(new File("layout.xml"), null, null, null, null);

    return Collections.singletonList(layout);
  }
}