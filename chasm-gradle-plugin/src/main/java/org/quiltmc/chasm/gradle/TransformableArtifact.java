package org.quiltmc.chasm.gradle;

import java.io.File;
import java.nio.file.Path;

import org.gradle.api.artifacts.ModuleVersionIdentifier;
import org.gradle.api.artifacts.ResolvedArtifact;

public class TransformableArtifact {
    private final Path path;
    private final ResolvedArtifact artifact;

    private boolean isTransformed = false;

    public TransformableArtifact(File file) {
        this.path = file.toPath();
        this.artifact = null;
    }

    public TransformableArtifact(ResolvedArtifact artifact) {
        this.path = artifact.getFile().toPath();
        this.artifact = artifact;
    }

    public ResolvedArtifact getArtifact() {
        return artifact;
    }

    public boolean isTransformed() {
        return isTransformed;
    }

    public void setTransformed() {
        isTransformed = true;
    }

    public Path getSourceFile() {
        return path;
    }

    public Path getTargetFile(Path baseDir, String hash) {
        // File artifact
        if (artifact == null) {
            String oldFileName = path.getFileName().toString();
            int index = oldFileName.lastIndexOf('.');
            index = index == -1 ? oldFileName.length() : index;
            String fileName = oldFileName.substring(0, index) + "-" + hash + oldFileName.substring(index);
            return baseDir.resolve(fileName);
        }

        StringBuilder fileName = new StringBuilder();
        ModuleVersionIdentifier module = artifact.getModuleVersion().getId();
        fileName.append(module.getName()).append('-').append(module.getVersion()).append('-').append(hash);
        if (artifact.getClassifier() != null) {
            fileName.append('-').append(artifact.getClassifier());
        }
        fileName.append('.').append(artifact.getExtension());

        return baseDir
                .resolve(module.getGroup().replace('.', '/'))
                .resolve(module.getName())
                .resolve(fileName.toString());
    }

    public boolean isJar() {
        if (artifact == null) {
            return path.endsWith(".jar");
        } else {
            return artifact.getExtension().equals("jar");
        }
    }
}
