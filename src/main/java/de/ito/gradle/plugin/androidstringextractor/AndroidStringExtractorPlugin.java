package de.ito.gradle.plugin.androidstringextractor;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskContainer;

public class AndroidStringExtractorPlugin implements Plugin<Project> {

  static final String GROUP_NAME = "localization";
  static final String TASK_NAME = "localeExport";

  @Override
  public void apply(Project target) {
//      target.getExtensions().create("localization", StringExtractorConfig.class);
      TaskContainer tasks = target.getTasks();
      tasks.create(TASK_NAME, AndroidStringExtractorTask.class).setGroup(GROUP_NAME);
      tasks.create("normalizeStringFiles", AndroidStringExtractorTask.class).setGroup(GROUP_NAME);
  }
}
