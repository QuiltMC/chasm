package org.quiltmc.chasm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.quiltmc.chasm.asm.ChasmClassWriter;
import org.quiltmc.chasm.transformer.*;
import org.quiltmc.chasm.tree.LinkedHashMapNode;
import org.quiltmc.chasm.tree.ListNode;
import org.quiltmc.chasm.tree.MapNode;
import org.quiltmc.chasm.tree.Node;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ChasmProcessor {
    private final List<Transformer> transformers = new ArrayList<>();

    public void addTransformer(Transformer transformerNode) {
        transformers.add(transformerNode);
    }

    public void processJar(File inputFile, File outputFile) throws IOException {
        ZipFile inputJar = new ZipFile(inputFile);
        ZipOutputStream outputJar = new ZipOutputStream(Files.newOutputStream(outputFile.toPath()));

        List<ClassReader> classReaders = new ArrayList<>();

        // Read input jar
        for(ZipEntry entry : inputJar.stream().toList()) {
            if (entry.getName().endsWith(".class")) {
                classReaders.add(new ClassReader(inputJar.getInputStream(entry)));
            }
            else {
                // Skip directories
                if (!entry.isDirectory()) {
                    // Copy non-class files directly
                    outputJar.putNextEntry(entry);
                    inputJar.getInputStream(entry).transferTo(outputJar);
                }
            }
        }

        // Generate class tree
        LinkedHashMapNode classes = new LinkedHashMapNode();
        classes.initializePath(new NodePath());
        for (ClassReader reader : classReaders) {
            LazyClassNode node = new LazyClassNode(reader, classes.getPath().append(reader.getClassName()));
            classes.put(reader.getClassName(), node);
        }

        // Apply transformers
        TransformationSorter transformations = new TransformationSorter();
        for (Transformer transformer : transformers) {
            transformations.addAll(transformer.apply(classes));
        }

        // Add slices to manager
        SliceManager sliceManager = new SliceManager(classes);
        for (Transformation transformation : transformations.get()) {
            if (transformation.getTarget() instanceof SliceTarget sliceTarget) {
                sliceManager.addSlice(sliceTarget);
            }

            for (Target target : transformation.getSources().values()) {
                if (target instanceof SliceTarget sliceTarget) {
                    sliceManager.addSlice(sliceTarget);
                }
            }
        }

        // Apply transformations
        for (Transformation transformation : transformations.get()) {
            // Read requested targets
            Target target = transformation.getTarget();
            Node resolvedTarget = target.resolve(classes);
            MapNode resolvedSources = new LinkedHashMapNode();
            for (Map.Entry<String, Target> source : transformation.getSources().entrySet()) {
                resolvedSources.put(source.getKey(), source.getValue().resolve(classes));
            }

            // Create replacement node
            Node replacementNode = transformation.apply(resolvedTarget, resolvedSources);

            // Insert result
            if (target instanceof NodeTarget nodeTarget) {
                Node parentNode = nodeTarget.getPath().getParent().resolve(classes);
                Object index = nodeTarget.getPath().getLastEntry();
                if (parentNode instanceof MapNode mapNode && index instanceof String key) {
                    mapNode.put(key, replacementNode);
                }
                else if (parentNode instanceof ListNode listNode && index instanceof Integer i) {
                    listNode.remove((int)i);
                    listNode.add(i, replacementNode);
                }
                else {
                    throw new UnsupportedOperationException("Invalid index into node.");
                }
            }
            else if (target instanceof SliceTarget sliceTarget && replacementNode instanceof ListNode listNode) {
                sliceManager.replaceSlice(sliceTarget, listNode);
            }
            else {
                throw new RuntimeException("Can't insert replacement");
            }
        }

        // Write output jar
        for (Node node : classes.values()) {
            if (node instanceof MapNode mapNode) {
                ChasmClassWriter chasmWriter = new ChasmClassWriter(mapNode);

                ClassWriter writer = new ClassWriter(null, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
                chasmWriter.accept(writer);

                outputJar.putNextEntry(new ZipEntry(mapNode.get("name") + ".class"));
                outputJar.write(writer.toByteArray());
            }
            else {
                throw new RuntimeException("Invalid class node.");
            }
        }
    }
}
