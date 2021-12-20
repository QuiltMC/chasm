package org.quiltmc.chasm.internal.asm.visitor;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.TypePath;
import org.quiltmc.chasm.api.tree.ArrayListNode;
import org.quiltmc.chasm.api.tree.LinkedHashMapNode;
import org.quiltmc.chasm.api.tree.ListNode;
import org.quiltmc.chasm.api.tree.MapNode;
import org.quiltmc.chasm.api.tree.WrapperValueNode;
import org.quiltmc.chasm.internal.util.NodeConstants;

public class ChasmFieldVisitor extends FieldVisitor {

    private final ListNode annotations = new ArrayListNode();
    private final ListNode attributes = new ArrayListNode();

    public ChasmFieldVisitor(int api, MapNode fieldNode) {
        super(api);

        fieldNode.put(NodeConstants.ANNOTATIONS, annotations);
        fieldNode.put(NodeConstants.ATTRIBUTES, attributes);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        MapNode annotation = new LinkedHashMapNode();
        ListNode values = new ArrayListNode();
        annotation.put(NodeConstants.DESCRIPTOR, new WrapperValueNode(descriptor));
        annotation.put(NodeConstants.VISIBLE, new WrapperValueNode(visible));
        annotation.put(NodeConstants.VALUES, values);
        annotations.add(annotation);

        return new ChasmAnnotationVisitor(api, values);
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        MapNode annotation = new LinkedHashMapNode();
        ListNode values = new ArrayListNode();
        annotation.put(NodeConstants.DESCRIPTOR, new WrapperValueNode(descriptor));
        annotation.put(NodeConstants.VISIBLE, new WrapperValueNode(visible));
        annotation.put(NodeConstants.VALUES, new WrapperValueNode(values));
        annotation.put(NodeConstants.TYPE_REF, new WrapperValueNode(typeRef));
        annotation.put(NodeConstants.TYPE_PATH, new WrapperValueNode(typePath.toString()));
        annotations.add(annotation);

        return new ChasmAnnotationVisitor(api, values);
    }

    @Override
    public void visitAttribute(Attribute attribute) {
        attributes.add(new WrapperValueNode(attribute));
    }

    @Override
    public void visitEnd() {
        // Nothing to do here
    }
}
