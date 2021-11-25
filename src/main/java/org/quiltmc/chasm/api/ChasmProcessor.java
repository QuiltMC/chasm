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

public class ChasmProcessor {
    private final SuperClassProvider superClassProvider;

    private final ListNode classes;
    private final List<Transformer> transformers = new ArrayList<>();

    public ChasmProcessor(SuperClassProvider superClassProvider) {
        this.superClassProvider = superClassProvider;
        classes = new ArrayListNode();
    }

    public void addTransformer(Transformer transformer) {
        transformers.add(transformer);
    }

    public void addClass(byte[] classBytes) {
        ClassReader classReader = new ClassReader(classBytes);
        LazyClassNode classNode = new LazyClassNode(classReader);
        classes.add(classNode);
    }

    public List<byte[]> process() {
        PathInitializer.initialize(classes, new PathMetadata());

        List<List<Transformer>> rounds = TransformerSorter.sort(transformers);
        for (List<Transformer> round : rounds) {
            List<Transformation> transformations = applyTransformers(round, classes);
            List<Transformation> sorted = TransformationSorter.sort(transformations);

            TransformationApplier transformationApplier = new TransformationApplier(classes, sorted);
            transformationApplier.applyAll();
        }

        List<byte[]> classBytes = new ArrayList<>();
        for (Node node : classes) {
            MapNode classNode = (MapNode) node;

            ClassNodeReader chasmWriter = new ClassNodeReader(classNode);
            ClassWriter classWriter = new ChasmClassWriter(new ChasmSuperClassProvider(superClassProvider, classes));
            chasmWriter.accept(classWriter);
            classBytes.add(classWriter.toByteArray());
        }

        return classBytes;
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
