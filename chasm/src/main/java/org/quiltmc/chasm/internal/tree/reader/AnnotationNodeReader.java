package org.quiltmc.chasm.internal.tree.reader;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.TypePath;
import org.quiltmc.chasm.api.tree.ListNode;
import org.quiltmc.chasm.api.tree.MapNode;
import org.quiltmc.chasm.api.tree.Node;
import org.quiltmc.chasm.api.tree.ValueNode;
import org.quiltmc.chasm.internal.util.NodeConstants;

public class AnnotationNodeReader {
    private final Node annotationNode;

    public AnnotationNodeReader(Node annotationNode) {
        this.annotationNode = annotationNode;
    }

    @SuppressWarnings("ConstantConditions")
    public void visitAnnotation(AnnotationVisitor visitor) {
        ListNode values;
        if (annotationNode instanceof MapNode) {
            values = (ListNode) ((MapNode) annotationNode).get(NodeConstants.VALUES);
        } else {
            values = (ListNode) annotationNode;
        }
        if (values == null) {
            visitor.visitEnd();
            return;
        }

        for (Node value : values) {
            String name = null;
            if (value instanceof MapNode && ((MapNode) value).containsKey(NodeConstants.NAME)) {
                MapNode mapNode = (MapNode) value;
                // Name-value pairs
                name = ((ValueNode) mapNode.get(NodeConstants.NAME)).getValueAsString();
                value = mapNode.get(NodeConstants.VALUE);
            }

            if (value instanceof ValueNode) {
                visitor.visit(name, ((ValueNode) value).getValue());
            } else if (value instanceof ListNode) {
                AnnotationVisitor arrayVisitor = visitor.visitArray(name);

                new AnnotationNodeReader(value).visitAnnotation(arrayVisitor);
            } else {
                MapNode mapNode = (MapNode) value;
                if (mapNode.containsKey(NodeConstants.VALUE)) {
                    String descriptor = ((ValueNode) mapNode.get(NodeConstants.DESCRIPTOR)).getValueAsString();
                    String enumValue = ((ValueNode) mapNode.get(NodeConstants.VALUE)).getValueAsString();

                    visitor.visitEnum(name, descriptor, enumValue);
                } else {
                    String descriptor = ((ValueNode) mapNode.get(NodeConstants.DESCRIPTOR)).getValueAsString();
                    ListNode annotationValues = (ListNode) mapNode.get(NodeConstants.VALUES);

                    AnnotationVisitor annotationVisitor = visitor.visitAnnotation(name, descriptor);
                    new AnnotationNodeReader(annotationValues).visitAnnotation(annotationVisitor);
                }
            }
        }

        visitor.visitEnd();
    }

    public void visitAnnotation(VisitAnnotation visitAnnotation, VisitTypeAnnotation visitTypeAnnotation) {
        ValueNode annotationDesc = (ValueNode) ((MapNode) annotationNode).get(NodeConstants.DESCRIPTOR);
        ValueNode visible = (ValueNode) ((MapNode) annotationNode).get(NodeConstants.VISIBLE);
        ValueNode typeRef = (ValueNode) ((MapNode) annotationNode).get(NodeConstants.TYPE_REF);
        ValueNode typePath = (ValueNode) ((MapNode) annotationNode).get(NodeConstants.TYPE_PATH);
        AnnotationVisitor annotationVisitor;
        if (typeRef == null) {
            annotationVisitor = visitAnnotation.visitAnnotation(annotationDesc.getValueAsString(), visible.getValueAsBoolean());
        } else {
            annotationVisitor = visitTypeAnnotation.visitTypeAnnotation(typeRef.getValueAsInt(),
                    TypePath.fromString(typePath.getValueAsString()), annotationDesc.getValueAsString(),
                    visible.getValueAsBoolean());
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
