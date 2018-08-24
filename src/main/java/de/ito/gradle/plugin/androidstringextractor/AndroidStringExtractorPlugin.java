package de.ito.gradle.plugin.androidstringextractor;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class AndroidStringExtractorPlugin implements Plugin<Project> {

  static final String TASK_NAME = "localeExport";

  @Override
  public void apply(Project target) {
//        target.getExtensions().create("localization", new ConfigExtension());
      target.getTasks().create(TASK_NAME, AndroidStringExtractorTask.class);
  }
}
