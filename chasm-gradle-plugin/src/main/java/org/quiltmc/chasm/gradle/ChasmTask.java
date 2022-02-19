package org.quiltmc.chasm.gradle;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.LenientConfiguration;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.artifacts.ResolvedConfiguration;
import org.gradle.api.artifacts.ResolvedDependency;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

public abstract class ChasmTask extends DefaultTask {
    private final Project project;

    @Inject
    public ChasmTask(Project project) {
        this.project = project;
    }

    @Input
    public abstract Property<Path> getChasmDirectory();

    @Input
    public abstract Property<Configuration> getInputConfiguration();

    @Input
    public abstract Property<Configuration> getOutputConfiguration();

    @TaskAction
    public void run() {
        // Resolve the input configuration
        ResolvedConfiguration resolvedConfiguration = getInputConfiguration().get().getResolvedConfiguration();
        LenientConfiguration lenientConfiguration = resolvedConfiguration.getLenientConfiguration();

        // Collect all remainingFiles of the configuration
        Set<File> remainingFiles = resolvedConfiguration.getFiles();

        // Collect all dependencies and artifacts
        Set<TransformableDependency> dependencies = new LinkedHashSet<>();
        Map<File, TransformableArtifact> artifacts = new LinkedHashMap<>();

        // Collect Module dependencies (both external and project dependencies)
        for (ResolvedDependency dependency : lenientConfiguration.getAllModuleDependencies()) {
            // Ignore dependencies without artifacts
            // If there are no artifacts we can't determine if it's a project dependency.
            if (dependency.getModuleArtifacts().isEmpty()) {
                continue;
            }

            // Create the dependency
            TransformableDependency transformableDependency = new TransformableDependency(dependency);
            dependencies.add(transformableDependency);

            // Collect its artifacts
            for (ResolvedArtifact artifact : dependency.getModuleArtifacts()) {
                // Remove the file from the remaining files
                remainingFiles.remove(artifact.getFile());

                // Ensure there's only TransformableArtifact per File
                TransformableArtifact transformableArtifact =
                        artifacts.computeIfAbsent(artifact.getFile(), file -> new TransformableArtifact(artifact));

                // Add artifact to dependency
                transformableDependency.addArtifact(transformableArtifact);
            }
        }

        // Collect remaining files as file dependencies
        for (File file : remainingFiles) {
            // Create the dependency
            TransformableDependency transformableDependency = new TransformableDependency();
            dependencies.add(transformableDependency);

            // Ensure there's only TransformableArtifact per File
            TransformableArtifact transformableArtifact = artifacts.computeIfAbsent(file, TransformableArtifact::new);

            // Add artifact to dependency
            transformableDependency.addArtifact(transformableArtifact);
        }

        // Run Chasm
        String hash;
        Path baseDir = getChasmDirectory().get();
        Path newJar = baseDir.resolve("new-chasm-classes.jar");
        try {
            ChasmRunner chasmRunner = new ChasmRunner(baseDir, newJar);
            chasmRunner.addArtifacts(artifacts.values());
            hash = chasmRunner.transform();

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        // Register chasm repository
        project.getRepositories().maven(maven -> {
            maven.setName("Chasm Repository");
            maven.setUrl(baseDir.toUri());
            maven.getMetadataSources().artifact();
        });

        // Create artifact dependencies
        for (TransformableDependency dependency : dependencies) {
            getOutputConfiguration().get().getDependencies()
                    .add(dependency.getOutputDependency(project, baseDir, hash));
        }

        // Create dependency on new classes
        getOutputConfiguration().get().getDependencies().add(project.getDependencies().create(project.files(newJar)));
    }
}
