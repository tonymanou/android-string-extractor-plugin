package de.ito.gradle.plugin.androidstringextractor;

import com.android.build.gradle.AppExtension;
import com.android.build.gradle.LibraryExtension;
import com.android.build.gradle.api.BaseVariant;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.internal.DefaultDomainObjectSet;
import org.gradle.api.tasks.TaskContainer;

import java.util.Arrays;
import java.util.stream.Collectors;

public class AndroidStringExtractorPlugin implements Plugin<Project> {

  static final String GROUP_NAME = "localization";
  static final String TASK_NAME = "localeExport";

  public static void main(String[] a) {
    String str = "\n    coucou\\nmon <b>bidule</b>,\n    comment\nvas-tu ?\n  ";
    String r = Arrays.stream(str.split("\\s*\n\\s*"))
        .filter(s -> !s.trim().isEmpty())
        .collect(Collectors.joining(" "));
    System.out.println("'" + r + "'");
  }

  @Override
  public void apply(Project target) {
//      target.getExtensions().create("localization", StringExtractorConfig.class);
    TaskContainer tasks = target.getTasks();

    tasks.create("normalizeStringFiles", AndroidStringExtractorTask.class).setGroup(GROUP_NAME);

    target.afterEvaluate(project -> getProjectBuildVariants(target).forEach(variant -> {
      String name = StringUtils.capitalize(variant.getName());
      AndroidStringExtractorTask task = tasks.create("export" + name + "StringsToCsv", AndroidStringExtractorTask.class);
      task.setGroup(GROUP_NAME);
    }));
  }

  private static DefaultDomainObjectSet<? extends BaseVariant> getProjectBuildVariants(Project project) {
    if (project.getPlugins().hasPlugin("android")) {
      final AppExtension appExtension = (AppExtension) project.getExtensions().getByName("android");
      return (DefaultDomainObjectSet<? extends BaseVariant>) appExtension.getApplicationVariants();
    } else if (project.getPlugins().hasPlugin("android-library")) {
      final LibraryExtension libraryExtension = (LibraryExtension) project.getExtensions().getByName("android-library");
      return libraryExtension.getLibraryVariants();
    } else {
      throw new GradleException("Set android build types first");
    }
  }
}
