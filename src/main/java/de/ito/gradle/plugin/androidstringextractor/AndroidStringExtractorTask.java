package de.ito.gradle.plugin.androidstringextractor;

import de.ito.gradle.plugin.androidstringextractor.internal.AndroidProjectFactory;
import de.ito.gradle.plugin.androidstringextractor.internal.StringExtractor;
import de.ito.gradle.plugin.androidstringextractor.internal.StringValues;
import de.ito.gradle.plugin.androidstringextractor.internal.io.CsvPrinter;
import de.ito.gradle.plugin.androidstringextractor.internal.io.Printer;
import de.ito.gradle.plugin.androidstringextractor.internal.io.XlsxPrinter;
import de.ito.gradle.plugin.androidstringextractor.internal.resource.PluralRes;
import de.ito.gradle.plugin.androidstringextractor.internal.resource.Res;
import de.ito.gradle.plugin.androidstringextractor.internal.resource.StringArrayRes;
import de.ito.gradle.plugin.androidstringextractor.internal.resource.StringRes;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class AndroidStringExtractorTask extends DefaultTask {

  private final StringExtractor stringExtractor;
  private Output exportOutput = Output.CSV;

  public AndroidStringExtractorTask() {
    stringExtractor = new StringExtractor(new AndroidProjectFactory());
  }

  @TaskAction
  public void extractStringsFromLayouts() throws Exception {
    String projectPath = getProject().getProjectDir().getPath();
    Map<String, Map<String, StringValues>> d = stringExtractor.extract(projectPath);

    Map<String, StringValues> mainData = d.get("main");
    String defaultLocale = "en";
    List<String> mainQualifiers = mainData.keySet().stream()
            .filter(lang -> lang != null && !defaultLocale.equals(lang))
            .distinct()
            .sorted()
            .collect(Collectors.toList());

    d.forEach((flavor, data) -> {
      if (!"main".equals(flavor)) {
        mainData.forEach((qualifier, mainValues) -> {
          StringValues values = data.get(qualifier);
          if (values == null) {
            data.put(qualifier, mainValues);
          } else {
            List<Res> resources = new ArrayList<>(values.getValues());
            Set<Res> target = values.getValues();
            target.clear();

            mainValues.getValues().forEach(res -> {
              Res overrideRes = resources.stream()
                      .filter(r -> r.getClass().isInstance(res) && r.getKey().equals(res.getKey()))
                      .findFirst().orElse(res);
              target.add(overrideRes);
            });
          }
        });
      }
    });

    d.forEach((flavor, data) -> {
      if (data.isEmpty()) {
        return;
      }

      Set<String> unionQualifiers = new HashSet<>(mainQualifiers);
      unionQualifiers.addAll(data.keySet());
      List<String> qualifiers = unionQualifiers.stream()
              .filter(lang -> lang != null && !defaultLocale.equals(lang))
              .sorted()
              .collect(Collectors.toList());

      List<String> headers = new ArrayList<>();
      headers.add("name");
      headers.add(defaultLocale + " (default)");
      headers.addAll(qualifiers);

      try (OutputStream out = new FileOutputStream(new File(projectPath, "strings_" + flavor + exportOutput.getExtension()))) {
        Printer printer;
        switch (exportOutput) {
          case XLS:
            printer = new XlsxPrinter(out, false);
            break;
          case XLSX:
            printer = new XlsxPrinter(out, true);
            break;
          default:
            printer = new CsvPrinter(out);
        }

        printer.addHeaderRow(headers.toArray(new String[headers.size()]));

        data.get(null).getValues().forEach(res -> {
          if (res instanceof StringRes) {
            printString(printer, (StringRes) res, data, qualifiers);
          } else if (res instanceof PluralRes) {
            printPlural(printer, (PluralRes) res, data, qualifiers);
          } else if (res instanceof StringArrayRes) {
            printStringArray(printer, (StringArrayRes) res, data, qualifiers);
          }
        });

        printer.writeToDisk();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });
  }

  public void setExportOutput(Output exportOutput) {
    this.exportOutput = exportOutput;
  }

  private void printString(Printer printer, StringRes res, Map<String, StringValues> data, List<String> qualifiers) {
    String key = res.getKey();
    String[] line = new String[qualifiers.size() + 2];
    line[0] = nonNull(key);
    line[1] = nonNull(res.getValue());

    int i = 2;
    for (String qualifier : qualifiers) {
      Set<Res> values = data.get(qualifier).getValues();
      Res found = values.stream().filter(r -> key.equals(r.getKey()))
              .findFirst().orElse(new StringRes(key, null));

      if (found instanceof StringRes) {
        line[i] = nonNull(((StringRes) found).getValue());
      } else {
        throw new ClassCastException("not a string res");
      }
      i++;
    }
    printer.addRow(line);
  }

  private void printPlural(Printer printer, PluralRes res, Map<String, StringValues> data, List<String> qualifiers) {
    String key = res.getKey();
    Arrays.stream(PluralRes.Quantity.values()).forEach(quantity -> {
      String[] strings = new String[qualifiers.size() + 1];
      strings[0] = res.getValues().get(quantity);
      int i = 1;
      for (String qualifier : qualifiers) {
        Set<Res> values = data.get(qualifier).getValues();
        Res found = values.stream().filter(r -> key.equals(r.getKey()))
                .findFirst().orElse(new PluralRes(key));

        if (found instanceof PluralRes) {
          strings[i] = ((PluralRes) found).getValues().get(quantity);
        } else {
          throw new ClassCastException("not a plurals res");
        }
        i++;
      }

      if (Arrays.stream(strings).anyMatch(s -> s != null && !s.isEmpty())) {
        String[] line = new String[qualifiers.size() + 2];
        line[0] = key + "[" + quantity + "]";
        for (int j = 0, max = strings.length; j < max; j++) {
          line[j + 1] = nonNull(strings[j]);
        }
        printer.addRow(line);
      }
    });
  }

  private void printStringArray(Printer printer, StringArrayRes res, Map<String, StringValues> data, List<String> qualifiers) {
    String key = res.getKey();
    List<List<String>> lists = new ArrayList<>(qualifiers.size() + 1);
    lists.add(res.getValues());
    for (String qualifier : qualifiers) {
      Set<Res> values = data.get(qualifier).getValues();
      Res found = values.stream().filter(r -> key.equals(r.getKey()))
              .findFirst().orElse(new StringArrayRes(key));

      if (found instanceof StringArrayRes) {
        lists.add(((StringArrayRes) found).getValues());
      } else {
        throw new ClassCastException("not a string array res");
      }
    }

    int max = lists.stream().map(List::size).max(Comparator.naturalOrder()).orElse(0);
    for (int index = 0; index < max; index++) {
      String[] line = new String[qualifiers.size() + 2];
      line[0] = key + "[]";

      int i = 1;
      for (List<String> list : lists) {
        if (list.size() > index) {
          line[i] = nonNull(list.get(index));
        } else {
          line[i] = "";
        }
        i++;
      }
      printer.addRow(line);
    }
  }

  private static String nonNull(String s) {
    return s == null ? "" : s;
  }

  public enum Output {
    CSV, XLS, XLSX;

    String getExtension() {
      return "." + name().toLowerCase();
    }
  }
}