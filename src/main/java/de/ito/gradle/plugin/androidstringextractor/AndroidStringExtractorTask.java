package de.ito.gradle.plugin.androidstringextractor;

import de.ito.gradle.plugin.androidstringextractor.internal.AndroidProjectFactory;
import de.ito.gradle.plugin.androidstringextractor.internal.StringExtractor;
import de.ito.gradle.plugin.androidstringextractor.internal.StringValues;
import de.ito.gradle.plugin.androidstringextractor.internal.resource.PluralRes;
import de.ito.gradle.plugin.androidstringextractor.internal.resource.StringArrayRes;
import de.ito.gradle.plugin.androidstringextractor.internal.resource.StringRes;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.util.Map;

public class AndroidStringExtractorTask extends DefaultTask {

  private final StringExtractor stringExtractor;

  public AndroidStringExtractorTask() {
    stringExtractor = new StringExtractor(new AndroidProjectFactory());
  }

  @TaskAction
  public void extractStringsFromLayouts() throws Exception {
    String projectPath = getProject().getProjectDir().getPath();
    Map<String, Map<String, StringValues>> d = stringExtractor.extract(projectPath);
    d.forEach((flavor, data) ->
            data.forEach((qualifier, values) -> values.getValues().forEach(res -> {
              String vq = qualifier == null ? "" : "[" + qualifier + "]";
              String value;
              if (res instanceof StringRes) {
                value = ((StringRes) res).getValue();
              } else if (res instanceof PluralRes) {
                value = ((PluralRes) res).values.entrySet().stream()
                        .map(entry -> "[" + entry.getKey() + "] " + entry.getValue())
                        .reduce("", (u, v) -> u + ", " + v);
              } else if (res instanceof StringArrayRes) {
                value = ((StringArrayRes) res).values.stream()
                        .reduce("", (u, v) -> u + ", " + v);
              } else {
                value = "???";
              }
              getLogger().lifecycle(flavor + " STRING" + vq + ": " + res.getKey() + " = " + value);
            })));
  }
}