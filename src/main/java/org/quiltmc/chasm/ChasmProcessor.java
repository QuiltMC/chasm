package org.quiltmc.chasm;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.quiltmc.chasm.asm.writer.ChasmClassWriter;
import org.quiltmc.chasm.transformer.NodePath;
import org.quiltmc.chasm.transformer.Transformation;
import org.quiltmc.chasm.transformer.Transformer;
import org.quiltmc.chasm.tree.LinkedListNode;
import org.quiltmc.chasm.tree.ListNode;
import org.quiltmc.chasm.tree.MapNode;
import org.quiltmc.chasm.tree.Node;

public class ChasmProcessor {
    private final List<Transformer> transformers = new ArrayList<>();

    private final ListNode classes;

    public ChasmProcessor() {
        classes = new LinkedListNode();
        classes.initializePath(new NodePath());
    }

    public void addTransformer(Transformer transformer) {
        transformers.add(transformer);
    }

    public void addClass(byte[] classBytes) {
        ClassReader classReader = new ClassReader(classBytes);
        String className = classReader.getClassName();
        LazyClassNode classNode = new LazyClassNode(classReader, new NodePath().append(classes.size()));
        classes.add(classNode);
    }

    public List<byte[]> process() {
        List<List<Transformer>> rounds = sortTransformers();

        for (List<Transformer> round : rounds) {
            ListNode initialClasses = classes.toImmutable();

            List<Transformation> transformations = applyTransformers(round, initialClasses);
            List<Transformation> sorted = sortTransformations(transformations);

            TransformationApplier transformationApplier = new TransformationApplier(classes, sorted);
            transformationApplier.applyAll();
        }

        List<byte[]> classBytes = new ArrayList<>();
        for (Node node : classes) {
            MapNode classNode = (MapNode) node;

            ChasmClassWriter chasmWriter = new ChasmClassWriter(classNode);
            ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            chasmWriter.accept(classWriter);
            classBytes.add(classWriter.toByteArray());
        }

        return classBytes;
    }

    private List<List<Transformer>> sortTransformers() {
        // TODO: Sort transformers based on declared dependencies
        return List.of(transformers);
    }

    private List<Transformation> applyTransformers(List<Transformer> transformers, ListNode classes) {
        List<Transformation> transformations = new ArrayList<>();

        for (Transformer transformer : transformers) {
            transformations.addAll(transformer.apply(classes));
        }

        return transformations;
    }

    private List<Transformation> sortTransformations(List<Transformation> transformations) {
        TransformationSorter sorter = new TransformationSorter();
        sorter.addAll(transformations);
        return sorter.get();
    }
}
