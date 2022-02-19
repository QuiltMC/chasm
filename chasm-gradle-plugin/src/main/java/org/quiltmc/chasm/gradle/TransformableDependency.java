package org.quiltmc.chasm.gradle;

import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.artifacts.ResolvedDependency;
import org.gradle.api.artifacts.component.ComponentIdentifier;
import org.gradle.api.artifacts.component.ProjectComponentIdentifier;

public class TransformableDependency {
    private final Set<TransformableArtifact> artifacts = new LinkedHashSet<>();
    private final ResolvedDependency dependency;

    public TransformableDependency() {
        this.dependency = null;
    }

    public TransformableDependency(ResolvedDependency dependency) {
        this.dependency = dependency;
    }

    public boolean addArtifact(TransformableArtifact artifact) {
        return artifacts.add(artifact);
    }

    public ComponentIdentifier getComponentIdentifier() {
        return artifacts.iterator().next().getArtifact().getId().getComponentIdentifier();
    }

    public boolean isTransformed() {
        return artifacts.stream().anyMatch(TransformableArtifact::isTransformed);
    }

    public Dependency getOutputDependency(Project project, Path baseDir, String hash) {
        if (dependency == null) {
            if (isTransformed()) {
                return project.getDependencies().create(project.files(
                        artifacts.stream().map(artifact -> artifact.getTargetFile(baseDir, hash)).toList()));
            } else {
                return project.getDependencies()
                        .create(project.files(artifacts.stream().map(TransformableArtifact::getSourceFile).toList()));
            }
        }

        if (isTransformed()) {
            String notation =
                    dependency.getModuleGroup() + ":" + dependency.getModuleName() + ":" + dependency.getModuleVersion()
                            + "-" + hash;
            ModuleDependency dependency = (ModuleDependency) project.getDependencies().create(notation);

            for (TransformableArtifact artifact : artifacts) {
                ResolvedArtifact source = artifact.getArtifact();
                dependency.addArtifact(dependency.artifact(target -> {
                    target.setName(source.getName());
                    if (source.getClassifier() != null) {
                        target.setClassifier(source.getClassifier());
                    }
                    target.setExtension(source.getExtension());
                    target.setType(source.getType());
                }));
            }

            return dependency;
        } else {
            ModuleDependency moduleDependency;
            if (getComponentIdentifier() instanceof ProjectComponentIdentifier projectId) {
                moduleDependency = (ModuleDependency) project.getDependencies()
                        .project(Map.of("path", projectId.getProjectPath(), "configuration",
                                dependency.getConfiguration()));
            } else {
                String notation = dependency.getModuleGroup() + ":" + dependency.getModuleName() + ":"
                        + dependency.getModuleVersion();
                moduleDependency = (ModuleDependency) project.getDependencies().create(notation);

                for (TransformableArtifact artifact : artifacts) {
                    ResolvedArtifact source = artifact.getArtifact();
                    moduleDependency.addArtifact(moduleDependency.artifact(target -> {
                        target.setName(source.getName());
                        if (source.getClassifier() != null) {
                            target.setClassifier(source.getClassifier());
                        }
                        target.setExtension(source.getExtension());
                        target.setType(source.getType());
                    }));
                }
            }
            moduleDependency.setTransitive(false);
            return moduleDependency;
        }
    }
}
