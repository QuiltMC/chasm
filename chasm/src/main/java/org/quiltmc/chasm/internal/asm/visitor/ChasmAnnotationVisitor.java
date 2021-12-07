package org.quiltmc.chasm.internal.asm.visitor;

import org.objectweb.asm.AnnotationVisitor;
import org.quiltmc.chasm.api.tree.ArrayListNode;
import org.quiltmc.chasm.api.tree.LinkedHashMapNode;
import org.quiltmc.chasm.api.tree.ListNode;
import org.quiltmc.chasm.api.tree.MapNode;
import org.quiltmc.chasm.api.tree.ValueNode;
import org.quiltmc.chasm.internal.util.NodeConstants;

public class ChasmAnnotationVisitor extends AnnotationVisitor {
    private final ListNode values;

    public ChasmAnnotationVisitor(int api, ListNode values) {
        this(api, values, null);
    }

    public ChasmAnnotationVisitor(int api, ListNode values, AnnotationVisitor av) {
        super(api, av);

        this.values = values;
    }

    @Override
    public void visit(String name, Object value) {
        if (value instanceof Object[]) {
            AnnotationVisitor visitor = visitArray(name);
            for (Object entry : (Object[]) value) {
                visitor.visit(null, entry);
            }
            visitor.visitEnd();
        } else if (name == null) {
            this.values.add(new ValueNode(value));
        } else {
            MapNode valueNode = new LinkedHashMapNode();
            valueNode.put(NodeConstants.NAME, new ValueNode(name));
            valueNode.put(NodeConstants.VALUE, new ValueNode(value));
            this.values.add(valueNode);
        }

        super.visit(name, value);
    }

    @Override
    public void visitEnum(String name, String descriptor, String value) {
        MapNode enumValueNode = new LinkedHashMapNode();
        enumValueNode.put(NodeConstants.DESCRIPTOR, new ValueNode(descriptor));
        enumValueNode.put(NodeConstants.VALUE, new ValueNode(value));

        if (name != null) {
            MapNode valueNode = new LinkedHashMapNode();
            valueNode.put(NodeConstants.NAME, new ValueNode(name));
            valueNode.put(NodeConstants.VALUE, enumValueNode);
            this.values.add(valueNode);
        } else {
            this.values.add(enumValueNode);
        }

        super.visitEnum(name, descriptor, value);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String name, String descriptor) {
        MapNode annotationValueNode = new LinkedHashMapNode();
        ListNode values = new ArrayListNode();
        annotationValueNode.put(NodeConstants.DESCRIPTOR, new ValueNode(descriptor));
        annotationValueNode.put(NodeConstants.VALUES, values);

        if (name != null) {
            MapNode valueNode = new LinkedHashMapNode();
            valueNode.put(NodeConstants.NAME, new ValueNode(name));
            valueNode.put(NodeConstants.VALUE, annotationValueNode);
            this.values.add(valueNode);
        } else {
            this.values.add(annotationValueNode);
        }

        return new ChasmAnnotationVisitor(api, values, super.visitAnnotation(name, descriptor));
    }

    @Override
    public AnnotationVisitor visitArray(String name) {
        ListNode values = new ArrayListNode();

        if (name != null) {
            MapNode valueNode = new LinkedHashMapNode();
            valueNode.put(NodeConstants.NAME, new ValueNode(name));
            valueNode.put(NodeConstants.VALUE, values);
            this.values.add(valueNode);
        } else {
            this.values.add(values);
        }

        return new ChasmAnnotationVisitor(api, values, super.visitArray(name));
    }

    @Override
    public void visitEnd() {
        // Nothing to do
        super.visitEnd();
    }
}
