package org.quiltmc.chasm.internal.tree.reader;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.TypePath;
import org.quiltmc.chasm.api.tree.ListNode;
import org.quiltmc.chasm.api.tree.MapNode;
import org.quiltmc.chasm.api.tree.Node;
import org.quiltmc.chasm.api.tree.ValueNode;
import org.quiltmc.chasm.internal.util.NodeConstants;

@SuppressWarnings("unchecked")
public class AnnotationNodeReader {
    private final Node annotationNode;

    public AnnotationNodeReader(Node annotationNode) {
        this.annotationNode = annotationNode;
    }

    @SuppressWarnings("ConstantConditions")
    public void visitAnnotation(AnnotationVisitor visitor) {
        ListNode<Node> values;
        if (annotationNode instanceof MapNode) {
            values = (ListNode<Node>) ((MapNode<Node>) annotationNode).get(NodeConstants.VALUES);
        } else {
            values = (ListNode<Node>) annotationNode;
        }
        if (values == null) {
            visitor.visitEnd();
            return;
        }

        for (Node value : values) {
            String name = null;
            if (value instanceof MapNode && ((MapNode<Node>) value).containsKey(NodeConstants.NAME)) {
                MapNode<Node> mapNode = (MapNode<Node>) value;
                // Name-value pairs
                name = ((ValueNode<String>) mapNode.get(NodeConstants.NAME)).getValue();
                value = mapNode.get(NodeConstants.VALUE);
            }

            if (value instanceof ValueNode) {
                visitor.visit(name, ((ValueNode<Object>) value).getValue());
            } else if (value instanceof ListNode) {
                AnnotationVisitor arrayVisitor = visitor.visitArray(name);

                new AnnotationNodeReader(value).visitAnnotation(arrayVisitor);
            } else {
                MapNode<Node> mapNode = (MapNode<Node>) value;
                if (mapNode.containsKey(NodeConstants.VALUE)) {
                    String descriptor = ((ValueNode<String>) mapNode.get(NodeConstants.DESCRIPTOR)).getValue();
                    String enumValue = ((ValueNode<String>) mapNode.get(NodeConstants.VALUE)).getValue();

                    visitor.visitEnum(name, descriptor, enumValue);
                } else {
                    String descriptor = ((ValueNode<String>) mapNode.get(NodeConstants.DESCRIPTOR)).getValue();
                    ListNode<Node> annotationValues = (ListNode<Node>) mapNode.get(NodeConstants.VALUES);

                    AnnotationVisitor annotationVisitor = visitor.visitAnnotation(name, descriptor);
                    new AnnotationNodeReader(annotationValues).visitAnnotation(annotationVisitor);
                }
            }
        }

        visitor.visitEnd();
    }

    public void visitAnnotation(VisitAnnotation visitAnnotation, VisitTypeAnnotation visitTypeAnnotation) {
        ValueNode<String> annotationDesc = (ValueNode<String>) ((MapNode<Node>) annotationNode)
                .get(NodeConstants.DESCRIPTOR);
        ValueNode<Boolean> visible = (ValueNode<Boolean>) ((MapNode<Node>) annotationNode).get(NodeConstants.VISIBLE);
        ValueNode<Integer> typeRef = (ValueNode<Integer>) ((MapNode<Node>) annotationNode).get(NodeConstants.TYPE_REF);
        ValueNode<String> typePath = (ValueNode<String>) ((MapNode<Node>) annotationNode).get(NodeConstants.TYPE_PATH);
        AnnotationVisitor annotationVisitor;
        if (typeRef == null) {
            annotationVisitor = visitAnnotation.visitAnnotation(annotationDesc.getValue(), visible.getValue());
        } else {
            annotationVisitor = visitTypeAnnotation.visitTypeAnnotation(typeRef.getValue(),
                    TypePath.fromString(typePath.getValue()), annotationDesc.getValue(),
                    visible.getValue());
        }
        visitAnnotation(annotationVisitor);
    }

    interface VisitAnnotation {
        AnnotationVisitor visitAnnotation(final String descriptor, final boolean visible);
    }

    interface VisitTypeAnnotation {
        AnnotationVisitor visitTypeAnnotation(final int typeRef, final TypePath typePath, final String descriptor,
                                              final boolean visible);
    }
}
