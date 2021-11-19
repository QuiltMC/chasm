package org.quiltmc.chasm.asm.writer;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.TypePath;
import org.quiltmc.chasm.NodeConstants;
import org.quiltmc.chasm.tree.ListNode;
import org.quiltmc.chasm.tree.MapNode;
import org.quiltmc.chasm.tree.Node;
import org.quiltmc.chasm.tree.ValueNode;

@SuppressWarnings("unchecked")
public class ChasmAnnotationWriter {
    private final MapNode annotationNode;

    public ChasmAnnotationWriter(MapNode annotationNode) {
        this.annotationNode = annotationNode;
    }

    @SuppressWarnings("ConstantConditions")
    public void visitAnnotation(AnnotationVisitor visitor) {
        ListNode values;
        if (annotationNode instanceof MapNode) {
            values = (ListNode) annotationNode.get(NodeConstants.VALUES);
        } else {
            values = (ListNode) annotationNode;
        }

        for (Node value : values) {
            String name = null;
            if (value instanceof MapNode mapNode && mapNode.containsKey(NodeConstants.NAME)) {
                // Name-value pairs
                name = ((ValueNode<String>) mapNode.get(NodeConstants.NAME)).getValue();
                value = mapNode.get(NodeConstants.VALUE);
            }

            if (value instanceof ValueNode) {
                visitor.visit(name, ((ValueNode<Object>) value).getValue());
            } else if (value instanceof ListNode) {
                AnnotationVisitor arrayVisitor = visitor.visitArray(name);
                visitAnnotation(arrayVisitor);
            } else {
                MapNode mapNode = (MapNode) value;
                if (mapNode.containsKey(NodeConstants.VALUE)) {
                    String descriptor = ((ValueNode<String>) mapNode.get(NodeConstants.DESCRIPTOR)).getValue();
                    String enumValue = ((ValueNode<String>) mapNode.get(NodeConstants.VALUE)).getValue();

                    visitor.visitEnum(name, descriptor, enumValue);
                } else {
                    String descriptor = ((ValueNode<String>) mapNode.get(NodeConstants.DESCRIPTOR)).getValue();
                    ListNode annotationValues = (ListNode) mapNode.get(NodeConstants.VALUES);

                    AnnotationVisitor annotationVisitor = visitor.visitAnnotation(name, descriptor);
                    visitAnnotation(annotationVisitor);
                }
            }
        }

        visitor.visitEnd();
    }

    public void visitAnnotation(VisitAnnotation visitAnnotation, VisitTypeAnnotation visitTypeAnnotation) {
        ValueNode<String> annotationDesc = (ValueNode<String>) annotationNode.get(NodeConstants.DESCRIPTOR);
        ValueNode<Boolean> visible = (ValueNode<Boolean>) annotationNode.get(NodeConstants.VISIBLE);
        ValueNode<Integer> typeRef = (ValueNode<Integer>) annotationNode.get(NodeConstants.TYPE_REF);
        ValueNode<String> typePath = (ValueNode<String>) annotationNode.get(NodeConstants.TYPE_PATH);
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
