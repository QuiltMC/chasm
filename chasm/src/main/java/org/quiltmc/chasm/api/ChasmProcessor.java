package org.quiltmc.chasm.api;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.quiltmc.chasm.api.metadata.MetadataProvider;
import org.quiltmc.chasm.api.tree.ArrayListNode;
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
import org.quiltmc.chasm.internal.util.PathInitializer;
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
     * Adds the passed class data to this {@link ChasmProcessor}'s
     * list of classes to transform.
     *
     * @param classData The data of the transformable class.
     */
    public void addClass(ClassData classData) {
        ClassReader classReader = new ClassReader(classData.getClassBytes());
        LazyClassNode classNode = new LazyClassNode(classReader, classInfoProvider, classData.getMetadataProvider());
        classes.add(classNode);
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
     * @param onlyChangedClasses If this method should only return classes that changed during transformation.
     * @return The resulting list of class data.
     */
    public List<ClassData> process(boolean onlyChangedClasses) {
        LOGGER.info("Processing {} classes...", classes.size());

        LOGGER.info("Initializing paths...");
        PathInitializer.initialize(classes, new PathMetadata());

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
        List<ClassData> classData = new ArrayList<>();
        for (Node node : classes) {
            // Skip unchanged (still lazy) class nodes if requested
            if (onlyChangedClasses && node instanceof LazyClassNode) {
                continue;
            }

            MapNode classNode = Node.asMap(node);

            ClassNodeReader chasmWriter = new ClassNodeReader(classNode);
            ClassWriter classWriter = new ChasmClassWriter(
                    classInfoProvider);
            chasmWriter.accept(classWriter);
            classData.add(new ClassData(classWriter.toByteArray(), classNode.getMetadata()));
        }

        LOGGER.info("Processing done!");
        return classData;
    }

    private List<Transformation> applyTransformers(List<Transformer> transformers, ListNode classes) {
        List<Transformation> transformations = new ArrayList<>();

        for (Transformer transformer : transformers) {
            // TODO: Replace copy with immutability
            transformations.addAll(transformer.apply(classes.copy()));
        }

        return transformations;
    }
}
