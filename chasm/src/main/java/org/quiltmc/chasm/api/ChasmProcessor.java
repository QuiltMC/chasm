package org.quiltmc.chasm.api;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.quiltmc.chasm.api.util.ClassInfoProvider;
import org.quiltmc.chasm.internal.ChasmClassInfoProvider;
import org.quiltmc.chasm.internal.TransformationApplier;
import org.quiltmc.chasm.internal.TransformationSorter;
import org.quiltmc.chasm.internal.TransformerSorter;
import org.quiltmc.chasm.internal.asm.ChasmClassWriter;
import org.quiltmc.chasm.internal.tree.ClassNode;
import org.quiltmc.chasm.internal.tree.reader.ClassNodeReader;
import org.quiltmc.chasm.lang.api.ast.ListNode;
import org.quiltmc.chasm.lang.api.ast.MapNode;
import org.quiltmc.chasm.lang.api.ast.Node;
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
     * Creates a new {@link ChasmProcessor} that uses the given {@link ClassInfoProvider}.
     *
     * @param classInfoProvider A {@code ClassInfoProvider} to supply parents of classes that are not being
     *            transformed.
     */
    public ChasmProcessor(ClassInfoProvider classInfoProvider) {
        classes = new ListNode(new ArrayList<>());
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
     * Adds the passed class data to this {@link ChasmProcessor}'s
     * list of classes to transform.
     *
     * @param classData The data of the transformable class.
     */
    public void addClass(ClassData classData) {
        ClassReader classReader = new ClassReader(classData.getClassBytes());
        ClassNode classNode = new ClassNode(classReader, classInfoProvider, classes.getEntries().size());
        classNode.getMetadata().putAll(classData.getMetadata());
        classes.getEntries().add(classNode);
    }


    /**
     * Transforms this {@link ChasmProcessor}'s list of classes according
     * to this {@code ChasmProcessor}'s list of {@link Transformer}s.
     * If you only want the classes returned, see {@link #process(boolean)}.
     *
     * @return The resulting list of class data including unchanged classes.
     */
    public List<ClassData> process() {
        return process(false);
    }

    /**
     * Transforms this {@link ChasmProcessor}'s list of classes according
     * to this {@code ChasmProcessor}'s list of {@link Transformer}s.
     *
     * @param onlyModifiedClasses If this method should only return classes that changed during transformation.
     * @return The resulting list of class data.
     */
    public List<ClassData> process(boolean onlyModifiedClasses) {
        LOGGER.info("Processing {} classes...", classes.getEntries().size());

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

        LOGGER.info("Writing {} classes...", classes.getEntries().size());
        List<ClassData> classData = new ArrayList<>();
        for (Node node : classes.getEntries()) {
            MapNode classNode = (MapNode) node;

            // Unmodified classes
            if (node instanceof ClassNode) {
                // Unmodified classes
                if (onlyModifiedClasses) {
                    // Skip if requested
                    continue;
                }
                ClassWriter classWriter = new ClassWriter(0);
                ((ClassNode) node).getClassReader().accept(classWriter, 0);
                classData.add(new ClassData(classWriter.toByteArray(), classNode.getMetadata()));
            } else {
                // ModifiedClasses
                ClassNodeReader chasmWriter = new ClassNodeReader(classNode);
                ClassWriter classWriter = new ChasmClassWriter(classInfoProvider);
                chasmWriter.accept(classWriter);
                classData.add(new ClassData(classWriter.toByteArray(), classNode.getMetadata()));
            }
        }

        LOGGER.info("Processing done!");
        return classData;
    }

    private List<Transformation> applyTransformers(List<Transformer> transformers, ListNode classes) {
        List<Transformation> transformations = new ArrayList<>();

        for (Transformer transformer : transformers) {
            // TODO: Replace copy with immutability
            transformations.addAll(transformer.apply(classes));
        }

        return transformations;
    }
}
