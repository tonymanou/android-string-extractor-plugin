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

    tasks.create("exportStringsToCsv", AndroidStringExtractorTask.class, task -> task.setGroup(GROUP_NAME));
    tasks.create("exportStringsToXlsx", AndroidStringExtractorTask.class, task -> {
      task.setExportToExcel(true);
      task.setGroup(GROUP_NAME);
    });
  }
}
