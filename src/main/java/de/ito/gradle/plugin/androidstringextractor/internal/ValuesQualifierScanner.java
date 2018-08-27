package de.ito.gradle.plugin.androidstringextractor.internal;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static de.ito.gradle.plugin.androidstringextractor.internal.Util.assertPathIsDirectory;

class ValuesQualifierScanner {
  private static final FilenameFilter QUALIFIED_VALUES_FILTER = new QualifiedValuesFilter();
  private static final String VALUES_DIR_PREFIX = "values-";

  List<String> scan(File flavorPath) {
    File resPath = new File(flavorPath, "/res");

    if(!resPath.exists()) return Collections.emptyList();

    assertPathIsDirectory(resPath);
    File[] valuesWithQualifierDirs = resPath.listFiles(QUALIFIED_VALUES_FILTER);

    return getQualifiers(valuesWithQualifierDirs);
  }

  private List<String> getQualifiers(File[] layoutFiles) {
    List<String> qualifiers = new ArrayList<>();

    for (File layoutFile : layoutFiles) {
      String name = layoutFile.getName();
      String qualifier = name.substring(VALUES_DIR_PREFIX.length());
      qualifiers.add(qualifier);
    }

    return qualifiers;
  }

  private static class QualifiedValuesFilter implements FilenameFilter {
    @Override public boolean accept(File dir, String name) {
      return new File(dir, name).isDirectory() && name.startsWith(VALUES_DIR_PREFIX);
    }
  }
}
