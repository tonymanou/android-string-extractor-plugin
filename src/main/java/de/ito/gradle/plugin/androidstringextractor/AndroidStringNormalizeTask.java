package de.ito.gradle.plugin.androidstringextractor;

import de.ito.gradle.plugin.androidstringextractor.internal.AndroidProjectFactory;
import de.ito.gradle.plugin.androidstringextractor.internal.StringExtractor;
import de.ito.gradle.plugin.androidstringextractor.internal.StringValues;
import de.ito.gradle.plugin.androidstringextractor.internal.resource.StringRes;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.util.Map;

public class AndroidStringNormalizeTask extends DefaultTask {

  private final StringExtractor stringExtractor;

  public AndroidStringNormalizeTask() {
    stringExtractor = new StringExtractor(new AndroidProjectFactory());
  }

  @TaskAction
  public void extractStringsFromLayouts() throws Exception {
    String projectPath = getProject().getProjectDir().getPath();
    Map<String, Map<String, StringValues>> v = stringExtractor.extract(projectPath);
    v.forEach((flavor, data) ->
            data.forEach((qualifier, values) -> values.getValues().forEach(res -> {
              String vq = qualifier == null ? "" : "[" + qualifier + "]";
              String value;
              if (res instanceof StringRes) {
                value = ((StringRes) res).getValue();
              } else {
                value = "???";
              }
              getLogger().lifecycle(flavor + " STRING" + vq + ": " + res.getKey() + " = " + value);
            })));
  }
}