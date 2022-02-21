package org.quiltmc.chasm.gradle;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.antlr.v4.runtime.CharStreams;
import org.objectweb.asm.ClassReader;
import org.quiltmc.chasm.api.ChasmProcessor;
import org.quiltmc.chasm.api.ClassData;
import org.quiltmc.chasm.api.metadata.Metadata;
import org.quiltmc.chasm.api.metadata.MetadataProvider;
import org.quiltmc.chasm.api.util.ClassLoaderClassInfoProvider;
import org.quiltmc.chasm.lang.ChasmLangTransformer;

public class ChasmRunner {
    private final Path baseDir;
    private final Path newJar;

    private Set<TransformableArtifact> jars = new LinkedHashSet<>();

    public ChasmRunner(Path baseDir, Path newJar) {
        this.baseDir = baseDir;
        this.newJar = newJar;
    }

    public void addArtifacts(Collection<TransformableArtifact> artifacts) {
        for (TransformableArtifact artifact : artifacts) {
            if (artifact.isJar()) {
                jars.add(artifact);
            }
        }
    }

    public String transform() throws IOException {
        // Create hasher
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        // Hash input jars
        for (TransformableArtifact jar : jars) {
            digest.digest(Files.readAllBytes(jar.getSourceFile()));
        }

        // Convert the digest to a short hash
        String hash = Base64.getUrlEncoder().encodeToString(digest.digest()).substring(0, 10);

        // Check if hash file exists. If so, skip transformation.
        Path hashFile = baseDir.resolve(hash);
        if (Files.exists(hashFile)) {
            for (TransformableArtifact jar : jars) {
                // If the transformed jar exists at the target location, it was transformed during the last run
                Path target = jar.getTargetFile(baseDir, hash);
                if (Files.exists(target)) {
                    jar.setTransformed();
                }
            }
            return hash;
        }

        // Delete old files
        if (Files.exists(baseDir)) {
            Files.walkFileTree(hashFile.getParent(), new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        }

        // Prepare Chasm
        // TODO: Use correct class loader for JDK libs
        ChasmProcessor chasmProcessor =
                new ChasmProcessor(new ClassLoaderClassInfoProvider(null, getClass().getClassLoader()));

        // Iterate all jars
        for (TransformableArtifact jar : jars) {
            // Add metadata to allow connecting a class to its original jar
            MetadataProvider metadataProvider = new MetadataProvider();
            metadataProvider.put(TransformableArtifactMetadata.class, new TransformableArtifactMetadata(jar));

            // Iterate the jar entries
            JarFile jarFile = new JarFile(jar.getSourceFile().toFile(), false);
            for (JarEntry jarEntry : jarFile.stream().toList()) {
                String name = jarEntry.getName();

                // Read class files
                if (name.endsWith(".class")) {
                    // Read class and pass it to the processor
                    byte[] classBytes = jarFile.getInputStream(jarEntry).readAllBytes();
                    ClassData classData = new ClassData(classBytes, metadataProvider);
                    chasmProcessor.addClass(classData);
                }

                // Read transformer files
                if (name.startsWith("org/quiltmc/chasm/transformers/") && name.endsWith(".chasm")) {
                    ChasmLangTransformer transformer =
                            ChasmLangTransformer.parse(CharStreams.fromStream(jarFile.getInputStream(jarEntry)));

                    chasmProcessor.addTransformer(transformer);
                }
            }
        }

        // TODO: Load transformers from main project

        // Process Chasm
        List<ClassData> transformedClasses = chasmProcessor.process(true);
        Map<TransformableArtifact, Map<String, ClassData>> artifactClasses = new LinkedHashMap<>();

        // Map classes to artifacts
        for (ClassData transformedClass : transformedClasses) {
            TransformableArtifact artifact =
                    transformedClass.getMetadataProvider().get(TransformableArtifactMetadata.class).getArtifact();

            Map<String, ClassData> classes = artifactClasses.computeIfAbsent(artifact, a -> new LinkedHashMap<>());
            ClassReader classReader = new ClassReader(transformedClass.getClassBytes());
            classes.put(classReader.getClassName() + ".class", transformedClass);
        }

        // Extract all classes that don't have a source artifact
        Map<String, ClassData> newClasses = artifactClasses.remove(null);

        // Iterate all transformed artifacts
        for (Map.Entry<TransformableArtifact, Map<String, ClassData>> entry : artifactClasses.entrySet()) {
            TransformableArtifact artifact = entry.getKey();
            Map<String, ClassData> classes = entry.getValue();

            // Mark the artifact as transformed
            artifact.setTransformed();

            // Open the output file
            Path target = artifact.getTargetFile(baseDir, hash);
            Files.createDirectories(target.getParent());
            ZipOutputStream outputStream = new ZipOutputStream(Files.newOutputStream(target));

            // Iterate the input file
            JarFile inputJar = new JarFile(artifact.getSourceFile().toFile());
            for (JarEntry jarEntry : inputJar.stream().toList()) {
                String fileName = jarEntry.getName();

                // If the file is in the transformed classes, use those bytes, otherwise take from the source file
                byte[] bytes;
                if (classes.containsKey(fileName)) {
                    bytes = classes.get(jarEntry.getName()).getClassBytes();
                } else {
                    bytes = inputJar.getInputStream(jarEntry).readAllBytes();
                }

                // Write the output bytes
                outputStream.putNextEntry(jarEntry);
                outputStream.write(bytes);
            }

            outputStream.close();
        }

        // Open the output file
        if (newClasses != null) {
            Files.createDirectories(newJar.getParent());
            ZipOutputStream outputStream = new ZipOutputStream(Files.newOutputStream(newJar));
            for (Map.Entry<String, ClassData> classData : newClasses.entrySet()) {
                outputStream.putNextEntry(new ZipEntry(classData.getKey()));
                outputStream.write(classData.getValue().getClassBytes());
            }

            outputStream.close();
        }

        // Create hash file as marker for the next run
        Files.createDirectories(hashFile.getParent());
        Files.createFile(hashFile);

        return hash;
    }

    static class TransformableArtifactMetadata implements Metadata {
        private final TransformableArtifact artifact;

        public TransformableArtifactMetadata(TransformableArtifact artifact) {
            this.artifact = artifact;
        }

        @Override
        public Metadata copy() {
            return new TransformableArtifactMetadata(artifact);
        }

        public TransformableArtifact getArtifact() {
            return artifact;
        }
    }
}
