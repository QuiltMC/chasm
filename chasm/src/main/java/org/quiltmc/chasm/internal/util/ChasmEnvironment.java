package org.quiltmc.chasm.internal.util;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.quiltmc.chasm.api.Transformer;
import org.quiltmc.chasm.internal.transformer.ChasmLangTransformer;
import org.quiltmc.chasm.lang.api.ast.Node;
import org.quiltmc.chasm.lang.api.eval.Evaluator;

public class ChasmEnvironment implements Closeable {
    private final List<Path> rootDirectories = new ArrayList<>();

    private final List<Closeable> toClose = new ArrayList<>();

    /**
     * Add a directory or jar to the classpath.
     *
     * @param path A path to a directory or jar file, representing the classpath entry.
     */
    public void addToClasspath(Path path) throws IOException {
        if (Files.isRegularFile(path) && path.toString().endsWith(".jar")) {
            FileSystem fileSystem = FileSystems.newFileSystem(path, (ClassLoader) null);
            fileSystem.getRootDirectories().forEach(rootDirectories::add);
            toClose.add(fileSystem);
        } else if (Files.isDirectory(path)) {
            rootDirectories.add(path);
        } else {
            throw new IllegalArgumentException("Path must be either jar or directory: " + path);
        }
    }

    public Collection<Transformer> createTransformers() throws IOException {
        Evaluator evaluator = Evaluator.create();

        Map<String, Transformer> transformers = new LinkedHashMap<>();

        for (Path rootDirectory : rootDirectories) {
            Path transformerRoot = rootDirectory.resolve("org/quiltmc/chasm/transformers");
            Stream<Path> fileStream = Files.find(transformerRoot, Integer.MAX_VALUE,
                    (path, attr) -> attr.isRegularFile() && path.toString().endsWith(".chasm")
            );
            Iterator<Path> chasmFiles = fileStream.iterator();
            while (chasmFiles.hasNext()) {
                Path path = chasmFiles.next();
                String id = transformerRoot.relativize(path).toString();
                Node node = Node.parse(path);
                ChasmLangTransformer transformer = new ChasmLangTransformer(id, node, evaluator);
                Transformer previous = transformers.put(id, transformer);
                if (previous != null) {
                    throw new RuntimeException("Duplicate chasm transformer: " + id);
                }
            }
            fileStream.close();
        }

        return transformers.values();
    }

    public void collectClasses() {
        for (Path rootDirectory : rootDirectories) {

        }
    }

    private int getJavaVersion() {
        String versionString = System.getProperty("java.version");
        String[] parts = versionString.split("\\.");
        if (parts[0].equals("1")) {
            // Pre java 9
            return Integer.parseInt(parts[1]);
        } else {
            return Integer.parseInt(parts[0]);
        }
    }

    @Override
    public void close() throws IOException {
        for (Closeable closeable : toClose) {
            closeable.close();
        }
    }
}
