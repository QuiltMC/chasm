package org.quiltmc.chasm.api;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.quiltmc.chasm.api.tree.ArrayListNode;
import org.quiltmc.chasm.api.tree.ListNode;
import org.quiltmc.chasm.api.tree.MapNode;
import org.quiltmc.chasm.api.tree.Node;
import org.quiltmc.chasm.api.util.SuperClassProvider;
import org.quiltmc.chasm.internal.ChasmSuperClassProvider;
import org.quiltmc.chasm.internal.LazyClassNode;
import org.quiltmc.chasm.internal.TransformationApplier;
import org.quiltmc.chasm.internal.TransformationSorter;
import org.quiltmc.chasm.internal.TransformerSorter;
import org.quiltmc.chasm.internal.asm.ChasmClassWriter;
import org.quiltmc.chasm.internal.metadata.PathMetadata;
import org.quiltmc.chasm.internal.tree.reader.ClassNodeReader;
import org.quiltmc.chasm.internal.util.PathInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Transform a list of classes, according to a {@link List} of {@link Transformer}s.
 */
public class ChasmProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChasmProcessor.class);

    private final SuperClassProvider superClassProvider;

    private final ListNode classes;
    private final List<Transformer> transformers = new ArrayList<>();

    /**
     * Create a new {@link ChasmProcessor} using the given {@link SuperClassProvider}.
     *
     * @param superClassProvider A {@link SuperClassProvider} to supply parents of classes that are not being
     *                               transformed.
     */
    public ChasmProcessor(SuperClassProvider superClassProvider) {
        this.superClassProvider = superClassProvider;
        this.classes = new ArrayListNode();
    }

    /**
     * Add the passed {@link Transformer} to this {@link ChasmProcessor}'s
     *           list of {@link Transformer}s.
     *
     * @param transformer A {@link Transformer} to add to this {@link ChasmProcessor}'s
     *           list of {@link Transformer}s to transform classes with.
     */
    public void addTransformer(Transformer transformer) {
        this.transformers.add(transformer);
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
        this.classes.add(classNode);
    }

    /**
     * Transform this {@link ChasmProcessor}'s list of classes according
     *          to this {@link ChasmProcessor}'s list of {@link Transformer}s.
     *
     * @return The list of transformed classes, as a {@link List} of {@code byte[]}s.
     */
    public List<byte[]> process() {
        LOGGER.info("Processing {} classes...", this.classes.size());

        LOGGER.info("Initializing paths...");
        PathInitializer.initialize(this.classes, new PathMetadata());

        LOGGER.info("Sorting {} transformers...", this.transformers.size());
        List<List<Transformer>> rounds = TransformerSorter.sort(this.transformers);

        LOGGER.info("Applying transformers in {} rounds:", rounds.size());
        for (List<Transformer> round : rounds) {
            LOGGER.info("Applying {} transformers...", round.size());
            List<Transformation> transformations = applyTransformers(round, this.classes);

            LOGGER.info("Sorting {} transformations...", transformations.size());
            List<Transformation> sorted = TransformationSorter.sort(transformations);

            LOGGER.info("Applying transformations...");
            TransformationApplier transformationApplier = new TransformationApplier(this.classes, sorted);
            transformationApplier.applyAll();
        }

        LOGGER.info("Writing {} classes...", this.classes.size());
        List<byte[]> classBytes = new ArrayList<>();
        for (Node node : this.classes) {
            MapNode classNode = (MapNode) node;

            ClassNodeReader chasmWriter = new ClassNodeReader(classNode);
            ClassWriter classWriter = new ChasmClassWriter(
                    new ChasmSuperClassProvider(this.superClassProvider, this.classes));
            chasmWriter.accept(classWriter);
            classBytes.add(classWriter.toByteArray());
        }

        LOGGER.info("Processing done!");
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
