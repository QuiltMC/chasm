package org.quiltmc.chasm.internal.asm.visitor;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.TypePath;
import org.quiltmc.chasm.internal.util.NodeConstants;
import org.quiltmc.chasm.lang.api.ast.BooleanNode;
import org.quiltmc.chasm.lang.api.ast.IntegerNode;
import org.quiltmc.chasm.lang.api.ast.ListNode;
import org.quiltmc.chasm.lang.api.ast.MapNode;
import org.quiltmc.chasm.lang.api.ast.StringNode;

public class ChasmFieldVisitor extends FieldVisitor {

    private final ListNode annotations = new ListNode(new ArrayList<>());

    public ChasmFieldVisitor(int api, MapNode fieldNode) {
        super(api);

        fieldNode.getEntries().put(NodeConstants.ANNOTATIONS, annotations);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        MapNode annotation = new MapNode(new LinkedHashMap<>());
        MapNode values = new MapNode(new LinkedHashMap<>());
        annotation.getEntries().put(NodeConstants.DESCRIPTOR, new StringNode(descriptor));
        annotation.getEntries().put(NodeConstants.VISIBLE, BooleanNode.from(visible));
        annotation.getEntries().put(NodeConstants.VALUES, values);
        annotations.getEntries().add(annotation);

        return new ChasmAnnotationVisitor(api, values);
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        MapNode annotation = new MapNode(new LinkedHashMap<>());
        MapNode values = new MapNode(new LinkedHashMap<>());
        annotation.getEntries().put(NodeConstants.DESCRIPTOR, new StringNode(descriptor));
        annotation.getEntries().put(NodeConstants.VISIBLE, BooleanNode.from(visible));
        annotation.getEntries().put(NodeConstants.VALUES, values);
        annotation.getEntries().put(NodeConstants.TYPE_REF, new IntegerNode(typeRef));
        annotation.getEntries().put(NodeConstants.TYPE_PATH, new StringNode(typePath.toString()));
        annotations.getEntries().add(annotation);

        return new ChasmAnnotationVisitor(api, values);
    }

    @Override
    public void visitAttribute(Attribute attribute) {
        throw new RuntimeException("Unknown attribute: " + attribute.type);
    }

    @Override
    public void visitEnd() {
        // Nothing to do here
    }
}
