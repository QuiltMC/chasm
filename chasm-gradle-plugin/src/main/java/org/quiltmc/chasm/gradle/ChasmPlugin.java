package org.quiltmc.chasm.gradle;


import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.gradle.TaskExecutionRequest;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.api.plugins.JavaPlugin;

/**
 * A plugin to transform the compilation classpath using Chasm.
 */
public class ChasmPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        // Force IDEA to run the Chasm task on sync
        if ("true".equals(project.getGradle().getStartParameter().getSystemPropertiesArgs().get("idea.sync.active"))) {
            project.getGradle().getStartParameter().getTaskRequests().add(new ChasmTaskExecutionRequest());
        }

        // This plugin requires the java plugin to be applied
        project.getPlugins().apply(JavaPlugin.class);

        // Obtain the configuration to modify and create a custom one for resolving
        Configuration compileClasspath =
                project.getConfigurations().getByName(JavaPlugin.COMPILE_CLASSPATH_CONFIGURATION_NAME);
        Configuration chasmCompileClasspath = project.getConfigurations().register("chasmCompileClasspath").get();

        // Obtain the Chasm cache directory
        Path chasmDir = project.getRootDir().toPath().resolve(".gradle").resolve("chasm");

        // Create and configure the Chasm task
        ChasmTask chasmTask = project.getTasks().create("chasm", ChasmTask.class, project);
        chasmTask.getChasmDirectory().set(chasmDir);
        chasmTask.getInputConfiguration().set(chasmCompileClasspath);
        chasmTask.getOutputConfiguration().set(compileClasspath);

        // Specify task dependencies for inter project dependencies.
        // This is done lazily in order to happen as late as possible.
        chasmTask.dependsOn(project.provider(() -> {
            // Move all super configurations into the chasm configuration.
            // This is done here since this is the latest possible callback point.
            chasmCompileClasspath.setExtendsFrom(compileClasspath.getExtendsFrom());
            compileClasspath.setExtendsFrom(Collections.emptySet());

            // Collect all project build dependencies for the configuration.
            // This ensures that subproject jars are available to Chasm.
            Set<Task> tasks = new LinkedHashSet<>();
            for (Dependency dependency : chasmCompileClasspath.getAllDependencies()) {
                if (dependency instanceof ProjectDependency projectDep) {
                    tasks.addAll(projectDep.getBuildDependencies().getDependencies(null));
                }
            }
            return tasks;
        }));

        // Ensure that chasm is run before java compilation
        project.getTasks().getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME).dependsOn(chasmTask);
    }

    static class ChasmTaskExecutionRequest implements TaskExecutionRequest {
        @Nonnull
        @Override
        public List<String> getArgs() {
            return List.of("chasm");
        }

        @Nullable
        @Override
        public String getProjectPath() {
            return null;
        }

        @Nullable
        @Override
        public File getRootDir() {
            return null;
        }
    }
}
