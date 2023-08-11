package org.quiltmc.chasm.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.quiltmc.chasm.api.util.Context;
import org.quiltmc.chasm.internal.ChasmContext;
import org.quiltmc.chasm.internal.ClassData;
import org.quiltmc.chasm.internal.TransformationApplier;
import org.quiltmc.chasm.internal.TransformationSorter;
import org.quiltmc.chasm.internal.TransformerSorter;
import org.quiltmc.chasm.internal.asm.ChasmClassWriter;
import org.quiltmc.chasm.internal.tree.ClassNode;
import org.quiltmc.chasm.internal.tree.reader.ClassNodeReader;
import org.quiltmc.chasm.internal.util.NodeConstants;
import org.quiltmc.chasm.internal.util.NodeUtils;
import org.quiltmc.chasm.lang.api.ast.Ast;
import org.quiltmc.chasm.lang.api.ast.ListNode;
import org.quiltmc.chasm.lang.api.ast.MapNode;
import org.quiltmc.chasm.lang.api.ast.Node;
import org.quiltmc.chasm.lang.api.metadata.Metadata;
import org.quiltmc.chasm.lang.internal.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Transforms the added classes using the added {@link Transformer}s.
 */
public class ChasmProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChasmProcessor.class);

    private final Context context;

    private final List<Transformer> transformers = new ArrayList<>();

    private final List<ClassData> classes = new ArrayList<>();

    /**
     * Creates a new {@link ChasmProcessor} that uses the given {@link Context}.
     *
     * @param context A {@code Context} to supply parents of classes that are not being
     *            transformed.
     */
    public ChasmProcessor(Context context) {
        this.context = context;
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
     * @param classBytes The bytes of the class.
     * @param metadata The metadata associated with the class.
     */
    public void addClass(byte @NotNull [] classBytes, @NotNull Metadata metadata) {
        this.classes.add(new ClassData(classBytes, metadata));
    }

    /**
     * Transforms the {@link ClassData} passed via {@link #addClass} using
     * the {@link Transformer Transformers} passed via {@link #addTransformer}.
     *
     * @return The transformed {@link ClassData}, wrapped in {@link ClassResult}.
     */
    public List<ClassResult> process() {
        LOGGER.info("Processing {} classes...", classes.size());

        // create a list to store ClassNodes
        ListNode classes = Ast.emptyList();
        // initialize a ChasmContext
        Context context = new ChasmContext(this.context, classes);
        // initialize a name-to-ClassData map
        Map<String, ClassData> nameToData = new HashMap<>();
        // turn ClassData's into ClassNodes
        for (ClassData classData : this.classes) {
            ClassReader classReader = new ClassReader(classData.getClassBytes());
            ClassNode classNode = new ClassNode(classReader, context, classes.size());
            classNode.getMetadata().putAll(classData.getMetadata());
            classes.add(classNode);

            if (nameToData.putIfAbsent(classReader.getClassName(), classData) != null) {
                throw new RuntimeException("Duplicate class: " + classReader.getClassName());
            }
        }

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
        List<ClassResult> result = new ArrayList<>();
        for (Node node : classes.getEntries()) {
            MapNode classNode = NodeUtils.asMap(node);
            String name = NodeUtils.getAsString(node, NodeConstants.NAME);
            ClassData classData = nameToData.remove(name);

            // Unmodified classes
            if (node instanceof ClassNode) {
                // Unmodified class
                Assert.check(classData != null);
                result.add(new ClassResult(
                        classData.getClassBytes(),
                        classData.getMetadata(),
                        ClassResult.Type.UNMODIFIED
                ));
            } else {
                // Modified or added class
                ClassNodeReader chasmWriter = new ClassNodeReader(classNode);
                ClassWriter classWriter = new ChasmClassWriter(context);
                chasmWriter.accept(classWriter);
                result.add(new ClassResult(
                        classWriter.toByteArray(),
                        classNode.getMetadata(),
                        classData == null ? ClassResult.Type.ADDED : ClassResult.Type.MODIFIED
                ));
            }
        }

        for (Map.Entry<String, ClassData> entry : nameToData.entrySet()) {
            result.add(new ClassResult(null, entry.getValue().getMetadata(), ClassResult.Type.REMOVED));
        }

        LOGGER.info("Processing done!");
        return result;
    }

    private List<Transformation> applyTransformers(List<Transformer> transformers, ListNode classes) {
        List<Transformation> transformations = new ArrayList<>();

        for (Transformer transformer : transformers) {
            // TODO: Replace copy with immutability
            transformations.addAll(transformer.apply(classes));
        }

        return transformations;
    }

    public Context getContext() {
        return context;
    }
}
