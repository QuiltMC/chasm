package org.quiltmc.chasm;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.quiltmc.chasm.asm.ChasmClassWriter;
import org.quiltmc.chasm.transformer.NodePath;
import org.quiltmc.chasm.transformer.NodeTarget;
import org.quiltmc.chasm.transformer.SliceTarget;
import org.quiltmc.chasm.transformer.Target;
import org.quiltmc.chasm.transformer.Transformation;
import org.quiltmc.chasm.transformer.Transformer;
import org.quiltmc.chasm.tree.LinkedHashMapNode;
import org.quiltmc.chasm.tree.ListNode;
import org.quiltmc.chasm.tree.MapNode;
import org.quiltmc.chasm.tree.Node;

public class ChasmProcessor {
    public static final String CLASS_FILE_EXTENSION = ".class";

    private final List<Transformer> transformers = new ArrayList<>();

    public void addTransformer(Transformer transformerNode) {
        transformers.add(transformerNode);
    }
    
    public void processJar(File inputFile, File outputFile) throws IOException {
        ZipFile inputJar = new ZipFile(inputFile);
        ZipOutputStream outputJar = new ZipOutputStream(Files.newOutputStream(outputFile.toPath()));

        List<ClassReader> classReaders = readInputJar(inputJar, outputJar);
        LinkedHashMapNode classes = generateClassTree(classReaders);

        // Apply transformers
        TransformationSorter transformations = new TransformationSorter();
        for (Transformer transformer : transformers) {
            transformations.addAll(transformer.apply(classes));
        }

        // Add slices to manager
        SliceManager sliceManager = new SliceManager(classes);
        sliceManager.addSlices(transformations);

        applyTransformations(classes, transformations, sliceManager);

        writeOutputJar(outputJar, classes);
    }

    private static List<ClassReader> readInputJar(ZipFile inputJar, ZipOutputStream outputJar)
            throws IOException {
        List<ClassReader> classReaders = new ArrayList<>();
        // Read input jar
        for(ZipEntry entry : inputJar.stream().toList()) {
            if (entry.getName().endsWith(CLASS_FILE_EXTENSION)) {
                classReaders.add(new ClassReader(inputJar.getInputStream(entry)));
            } else if (entry.isDirectory()) {
                // Skip directories
            } else {
                // Copy non-class files directly
                outputJar.putNextEntry(entry);
                inputJar.getInputStream(entry).transferTo(outputJar);
            }
        }
        return classReaders;
    }
    private static LinkedHashMapNode generateClassTree(List<ClassReader> classReaders) {
        // Generate class tree
        LinkedHashMapNode classes = new LinkedHashMapNode();
        classes.initializePath(new NodePath());
        for (ClassReader reader : classReaders) {
            LazyClassNode node = new LazyClassNode(reader, classes.getPath().append(reader.getClassName()));
            classes.put(reader.getClassName(), node);
        }
        return classes;
    }

    private static void insertTransformationResult(Target target,
                                                   LinkedHashMapNode classes,
                                                   SliceManager sliceManager,
                                                   Node replacementNode) {
        if (target instanceof NodeTarget nodeTarget) {
            Node parentNode = nodeTarget.getPath().getParent().resolve(classes);
            Object index = nodeTarget.getPath().getLastEntry();
            if (parentNode instanceof MapNode mapNode && index instanceof String key) {
                mapNode.put(key, replacementNode);
            } else if (parentNode instanceof ListNode listNode && index instanceof Integer i) {
                listNode.remove(i.intValue());
                listNode.add(i.intValue(), replacementNode);
            } else {
                throw new UnsupportedOperationException("Invalid index into node.");
            }
        } else if (target instanceof SliceTarget sliceTarget
                && replacementNode instanceof ListNode listNode) {
            sliceManager.replaceSlice(sliceTarget, listNode);
        } else {
            throw new RuntimeException("Can't insert replacement");
        }
    }

    private static MapNode resolveSources(LinkedHashMapNode classes,
                                          Transformation transformation) {
        MapNode resolvedSources = new LinkedHashMapNode();
        for (Map.Entry<String, Target> source : transformation.getSources().entrySet()) {
            resolvedSources.put(source.getKey(), source.getValue().resolve(classes));
        }
        return resolvedSources;
    }

    private static void applyTransformations(LinkedHashMapNode classes, TransformationSorter transformations,
                                      SliceManager sliceManager) {
        // Apply transformations
        for (Transformation transformation : transformations.get()) {
            // Read requested targets
            Target target = transformation.getTarget();
            Node resolvedTarget = target.resolve(classes);
            
            MapNode resolvedSources = resolveSources(classes, transformation);

            // Create replacement node
            Node replacementNode = transformation.apply(resolvedTarget, resolvedSources);

            // Insert result
            insertTransformationResult(target, classes, sliceManager, replacementNode);
        }
    }
    
    private static void writeOutputJar(ZipOutputStream outputJar,
                                       LinkedHashMapNode classes) throws IOException {
        // Write output jar
        for (Node node : classes.values()) {
            if (node instanceof MapNode mapNode) {
                ChasmClassWriter chasmWriter = new ChasmClassWriter(mapNode);

                ClassWriter writer = new ClassWriter(null, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
                chasmWriter.accept(writer);

                outputJar.putNextEntry(new ZipEntry(mapNode.get(NodeConstants.NAME) + CLASS_FILE_EXTENSION));
                outputJar.write(writer.toByteArray());
            } else {
                throw new RuntimeException("Invalid class node.");
            }
        }
    }

}
