package org.quiltmc.chasm.api;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.quiltmc.chasm.api.tree.LinkedListNode;
import org.quiltmc.chasm.api.tree.ListNode;
import org.quiltmc.chasm.api.tree.MapNode;
import org.quiltmc.chasm.api.tree.Node;
import org.quiltmc.chasm.internal.LazyClassNode;
import org.quiltmc.chasm.internal.TopologicalSorter;
import org.quiltmc.chasm.internal.TransformationApplier;
import org.quiltmc.chasm.internal.asm.writer.ChasmClassWriter;
import org.quiltmc.chasm.internal.metadata.PathMetadata;
import org.quiltmc.chasm.internal.util.PathInitializer;

/**
 * Transform a list of classes, according to a {@link List} of {@link Transformer}s.
 */
public class ChasmProcessor {
    private final List<Transformer> transformers = new ArrayList<>();

    private final ListNode classes;

    /**
     * Create a new, empty {@link ChasmProcessor}
     *   with no {@link Transformer}s and no classes to transform.
     */
    public ChasmProcessor() {
        classes = new LinkedListNode();
    }

    /**
     * Add the passed {@link Transformer} to this {@link ChasmProcessor}'s
     *           list of {@link Transformer}s.
     *
     * @param transformer A {@link Transformer} to add to this {@link ChasmProcessor}'s
     *           list of {@link Transformer}s to transform classes with.
     */
    public void addTransformer(Transformer transformer) {
        transformers.add(transformer);
    }

    /**
     * Add the passed class {@code byte[]} to this {@link ChasmProcessor}'s
     *          list of classes to transform.
     *
     * @param classBytes A class {@code byte[]} to transform.
     */
    public void addClass(byte[] classBytes) {
        ClassReader classReader = new ClassReader(classBytes);
        LazyClassNode classNode = new LazyClassNode(classReader);
        classes.add(classNode);
    }

    /**
     * Transform this {@link ChasmProcessor}'s list of classes according
     *          to this {@link ChasmProcessor}'s list of {@link Transformer}s.
     *
     * @return The list of transformed classes, as a {@link List} of {@code byte[]}s.
     */
    public List<byte[]> process() {
        PathInitializer.initialize(classes, new PathMetadata());

        List<List<Transformer>> rounds = TopologicalSorter.sortTransformers(transformers);
        for (List<Transformer> round : rounds) {
            List<Transformation> transformations = applyTransformers(round, classes);
            List<Transformation> sorted = TopologicalSorter.sortTransformations(transformations);

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

    private static List<Transformation> applyTransformers(List<Transformer> transformers, ListNode classes) {
        List<Transformation> transformations = new ArrayList<>();

        for (Transformer transformer : transformers) {
            // TODO: Replace copy with immutability
            transformations.addAll(transformer.apply(classes.copy()));
        }

        return transformations;
    }
}
