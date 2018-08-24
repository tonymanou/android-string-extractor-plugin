package de.ito.gradle.plugin.androidstringextractor;

import de.ito.gradle.plugin.androidstringextractor.internal.AndroidProjectFactory;
import de.ito.gradle.plugin.androidstringextractor.internal.StringExtractor;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

public class AndroidStringExtractorTask extends DefaultTask {

  private final StringExtractor stringExtractor;

  public AndroidStringExtractorTask() {
    stringExtractor = new StringExtractor(new AndroidProjectFactory());
  }

  @TaskAction
  public void extractStringsFromLayouts() throws Exception {
    String projectPath = getProject().getProjectDir().getPath();
    stringExtractor.extract(projectPath);
  }
}