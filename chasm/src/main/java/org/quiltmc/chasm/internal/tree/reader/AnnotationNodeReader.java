package org.quiltmc.chasm.internal.tree.reader;

import java.util.Map;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.TypePath;
import org.quiltmc.chasm.internal.util.NodeConstants;
import org.quiltmc.chasm.internal.util.NodeUtils;
import org.quiltmc.chasm.lang.api.ast.ListNode;
import org.quiltmc.chasm.lang.api.ast.MapNode;
import org.quiltmc.chasm.lang.api.ast.Node;

public class AnnotationNodeReader {
    private final Node annotationNode;

    public AnnotationNodeReader(Node annotationNode) {
        this.annotationNode = annotationNode;
    }

    public static void visitAnnotationValue(AnnotationVisitor visitor, String name, Node node) {
        if (node instanceof ListNode) {
            AnnotationVisitor arrayVisitor = visitor.visitArray(name);

            for (Node entry : NodeUtils.asList(node).getEntries()) {
                arrayVisitor.visit(null, entry);
            }
            arrayVisitor.visitEnd();
        } else if (node instanceof MapNode) {
            String type = NodeUtils.getAsString(node, NodeConstants.TYPE);
            String descriptor = NodeUtils.getAsString(node, NodeConstants.DESCRIPTOR);
            MapNode annotationValues = NodeUtils.getAsMap(node, NodeConstants.VALUES);
            String enumValue = NodeUtils.getAsString(node, NodeConstants.VALUE);

            if (type != null) {
                visitor.visit(name, NodeUtils.fromValueNode(node));
            } else if (descriptor != null && annotationValues != null) {
                AnnotationVisitor annotationVisitor = visitor.visitAnnotation(name, descriptor);
                visitValues(annotationVisitor, annotationValues);
            } else if (descriptor != null && enumValue != null) {
                visitor.visitEnum(name, descriptor, enumValue);
            } else {
                throw new RuntimeException("Invalid annotation value: " + node);
            }
        } else {
            throw new RuntimeException("Invalid annotation value: " + node);
        }
    }

    public static void visitValues(AnnotationVisitor visitor, Node node) {
        if (node instanceof MapNode) {
            for (Map.Entry<String, Node> entry : NodeUtils.asMap(node).getEntries().entrySet()) {
                visitAnnotationValue(visitor, entry.getKey(), entry.getValue());
            }
        } else if (node instanceof ListNode) {
            for (Node entry : NodeUtils.asList(node).getEntries()) {
                visitAnnotationValue(visitor, null, entry);
            }
        } else {
            throw new RuntimeException("Invalid annotation values: " + node);
        }

        visitor.visitEnd();
    }

    public void accept(AnnotationVisitorFactory factory, TypeAnnotationVisitorFactory typeFactory) {
        String descriptor = NodeUtils.getAsString(annotationNode, NodeConstants.DESCRIPTOR);
        boolean visible = NodeUtils.getAsBoolean(annotationNode, NodeConstants.VISIBLE);
        Long typeRef = NodeUtils.getAsLong(annotationNode, NodeConstants.TYPE_REF);
        String typePath = NodeUtils.getAsString(annotationNode, NodeConstants.TYPE_PATH);
        Node values = NodeUtils.get(annotationNode, NodeConstants.VALUES);

        AnnotationVisitor visitor;
        if (typePath != null) {
            visitor = typeFactory.create(typeRef.intValue(), TypePath.fromString(typePath), descriptor, visible);
        } else {
            visitor = factory.create(descriptor, visible);
        }

        visitValues(visitor, values);
    }

    public interface AnnotationVisitorFactory {
        AnnotationVisitor create(String descriptor, boolean visible);
    }

    public interface TypeAnnotationVisitorFactory {
        AnnotationVisitor create(int typeRef, TypePath typePath, String descriptor, boolean visible);
    }
}
