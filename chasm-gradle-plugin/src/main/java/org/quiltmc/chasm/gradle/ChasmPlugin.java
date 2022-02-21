package org.quiltmc.chasm.gradle;


import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.gradle.TaskExecutionRequest;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.compile.AbstractCompile;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.jvm.tasks.Jar;
import org.gradle.plugins.ide.idea.IdeaPlugin;

/**
 * A plugin to transform the compilation classpath using Chasm.
 */
public class ChasmPlugin implements Plugin<Project> {

    public static final String CHASM_TASK_NAME = "chasm";

    public static final String TRANSFORMER_JAR_TASK_NAME = "transformerJar";

    @Override
    public void apply(Project project) {
        // The Java plugin is required
        project.getPlugins().apply(JavaPlugin.class);

        // Get the classpath configuration
        Configuration compileClasspath =
                project.getConfigurations().getByName(JavaPlugin.COMPILE_CLASSPATH_CONFIGURATION_NAME);

        // Chasm cache directory
        Path chasmDir = project.getBuildDir().toPath().resolve("chasm");

        // Create jar containing the project's transformers
        TaskProvider<Jar> transformerJar = project.getTasks().register(TRANSFORMER_JAR_TASK_NAME, Jar.class, jar -> {
            Copy processResources = (Copy) project.getTasks().getByName(JavaPlugin.PROCESS_RESOURCES_TASK_NAME);
            jar.dependsOn(processResources);
            jar.from(project.fileTree(processResources.getDestinationDir()));
            jar.getArchiveFileName().set("Transformer.jar");
            jar.getDestinationDirectory().set(chasmDir.toFile());
            jar.include("org/quiltmc/chasm/transformers/**/*.chasm");
        });

        // Create configurations for chasm to work on
        Provider<Configuration> chasmInput = project.getConfigurations().register("chasmInput", configuration -> {
            configuration.setExtendsFrom(compileClasspath.getExtendsFrom());
            configuration.getDependencies().add(project.getDependencies().create(project.files(transformerJar)));
        });
        Provider<Configuration> chasmOutput = project.getConfigurations().register("chasmOutput");

        // Create and configure the Chasm task
        ChasmTask chasmTask = project.getTasks().create(CHASM_TASK_NAME, ChasmTask.class, project);
        chasmTask.getChasmDirectory().set(chasmDir.resolve("repository"));
        chasmTask.getInputConfiguration().set(chasmInput);
        chasmTask.getOutputConfiguration().set(chasmOutput);
        chasmTask.dependsOn(chasmInput.map(Configuration::getBuildDependencies));

        // Configure Compilation
        JavaCompile compileJava = (JavaCompile) project.getTasks().getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME);
        compileJava.dependsOn(chasmTask);
        compileJava.doFirst(new SetClasspathAction(chasmOutput));

        // Force IDEA to run the Chasm task on sync and set the classpath
        if ("true".equals(project.getGradle().getStartParameter().getSystemPropertiesArgs().get("idea.sync.active"))) {
            project.getGradle().getStartParameter().getTaskRequests().add(new ChasmTaskExecutionRequest());
            chasmTask.doLast(task -> compileClasspath.setExtendsFrom(Collections.singleton(chasmOutput.get())));
        }
    }

    static class SetClasspathAction implements Action<Task> {
        private final Provider<Configuration> classpath;

        public SetClasspathAction(Provider<Configuration> classpath) {
            this.classpath = classpath;
        }

        @Override
        public void execute(@Nonnull Task task) {
            ((AbstractCompile) task).setClasspath(classpath.get());
        }
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
