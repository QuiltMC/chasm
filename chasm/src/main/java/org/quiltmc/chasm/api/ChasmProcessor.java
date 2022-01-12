package org.quiltmc.chasm.api;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.quiltmc.chasm.api.tree.ArrayListNode;
import org.quiltmc.chasm.api.tree.FrozenListNode;
import org.quiltmc.chasm.api.tree.FrozenNode;
import org.quiltmc.chasm.api.tree.ListNode;
import org.quiltmc.chasm.api.tree.MapNode;
import org.quiltmc.chasm.api.tree.Node;
import org.quiltmc.chasm.api.util.ClassInfoProvider;
import org.quiltmc.chasm.internal.ChasmClassInfoProvider;
import org.quiltmc.chasm.internal.TransformationApplier;
import org.quiltmc.chasm.internal.TransformationSorter;
import org.quiltmc.chasm.internal.TransformerSorter;
import org.quiltmc.chasm.internal.asm.ChasmClassWriter;
import org.quiltmc.chasm.internal.metadata.PathMetadata;
import org.quiltmc.chasm.internal.tree.LazyClassNode;
import org.quiltmc.chasm.internal.tree.reader.ClassNodeReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Transforms the added classes using the added {@link Transformer}s.
 */
public class ChasmProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChasmProcessor.class);

    private final ClassInfoProvider classInfoProvider;

    private final ListNode classes;
    private final List<Transformer> transformers = new ArrayList<>();

    /**
     * Creates a new {@link ChasmProcessor} that uses the given {@link SuperClassProvider}.
     *
     * @param superClassProvider A {@code SuperClassProvider} to supply parents of classes that are not being
     *            transformed.
     */
    public ChasmProcessor(ClassInfoProvider classInfoProvider) {
        classes = new ArrayListNode();
        this.classInfoProvider = new ChasmClassInfoProvider(classInfoProvider, classes);
    }

    /**
     * Adds the passed {@link Transformer} to this {@link ChasmProcessor}'s
     * list of {@code Transformer}s.
     *
     * @param transformer A {@code Transformer} to add to this {@code ChasmProcessor}'s
     *            list of {@code Transformer}s to transform classes with.
     */
    public void addTransformer(Transformer transformer) {
        transformers.add(transformer);
    }

    /**
     * Adds the passed class {@code byte[]} to this {@link ChasmProcessor}'s
     * list of classes to transform.
     *
     * @param classBytes A transformable class as a {@code byte[]}.
     */
    public void addClass(byte[] classBytes) {
        ClassReader classReader = new ClassReader(classBytes);
        LazyClassNode classNode = new LazyClassNode(classReader, classInfoProvider);
        classes.add(classNode);
    }

    /**
     * Transforms this {@link ChasmProcessor}'s list of classes according
     * to this {@code ChasmProcessor}'s list of {@link Transformer}s.
     *
     * @return The resulting list of classes as {@code byte[]}s.
     */
    @SuppressWarnings("unchecked")
    public List<byte[]> process() {
        LOGGER.info("Processing {} classes...", classes.size());

        LOGGER.info("Initializing paths...");
        classes.updatePath(new PathMetadata());

        LOGGER.info("Sorting {} transformers...", transformers.size());
        List<List<Transformer>> rounds = TransformerSorter.sort(transformers);

        LOGGER.info("Applying transformers in {} rounds:", rounds.size());
        for (List<Transformer> round : rounds) {
            LOGGER.info("Applying {} transformers...", round.size());
            List<Transformation> transformations = applyTransformers(round, classes);

            LOGGER.info("Sorting {} transformations...", transformations.size());
            List<Transformation> sorted = TransformationSorter.sort(transformations);

            LOGGER.info("Applying transformations...");
            TransformationApplier transformationApplier = new TransformationApplier(classes, sorted);
            transformationApplier.applyAll();
        }

        LOGGER.info("Writing {} classes...", classes.size());
        List<byte[]> classBytes = new ArrayList<>();
        for (Node node : classes) {
            MapNode classNode = Node.asMap(node);

            ClassNodeReader chasmWriter = new ClassNodeReader(classNode);
            ClassWriter classWriter = new ChasmClassWriter(
                    classInfoProvider);
            chasmWriter.accept(classWriter);
            classBytes.add(classWriter.toByteArray());
        }

        LOGGER.info("Processing done!");
        return classBytes;
    }

    private List<Transformation> applyTransformers(List<Transformer> transformers, ListNode classes) {
        List<Transformation> transformations = new ArrayList<>();

        FrozenListNode frozenClasses = classes.asImmutable();
        for (Transformer transformer : transformers) {
            transformations.addAll(transformer.apply(frozenClasses));
        }

        return transformations;
    }
}
