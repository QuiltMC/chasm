package org.quiltmc.chasm.internal.tree.reader;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.quiltmc.chasm.internal.util.NodeConstants;
import org.quiltmc.chasm.internal.util.NodeUtils;
import org.quiltmc.chasm.lang.api.ast.ListNode;
import org.quiltmc.chasm.lang.api.ast.MapNode;
import org.quiltmc.chasm.lang.api.ast.Node;

public class FieldNodeReader {
    private final MapNode fieldNode;

    public FieldNodeReader(MapNode fieldNode) {
        this.fieldNode = fieldNode;
    }

    private void visitAnnotations(FieldVisitor visitor) {
        ListNode annotationsListNode = NodeUtils.getAsList(fieldNode, NodeConstants.ANNOTATIONS);
        if (annotationsListNode == null) {
            return;
        }
        for (Node n : annotationsListNode.getEntries()) {
            AnnotationNodeReader reader = new AnnotationNodeReader(n);
            reader.accept(visitor::visitAnnotation, visitor::visitTypeAnnotation);
        }
    }

    public void visitField(ClassVisitor visitor) {
        int access = NodeUtils.getAsInt(fieldNode, NodeConstants.ACCESS);
        String name = NodeUtils.getAsString(fieldNode, NodeConstants.NAME);
        String descriptor = NodeUtils.getAsString(fieldNode, NodeConstants.DESCRIPTOR);

        String signature = NodeUtils.getAsString(fieldNode, NodeConstants.SIGNATURE);

        Node valueNode = NodeUtils.get(fieldNode, NodeConstants.VALUE);
        Object value = valueNode == null ? null : NodeUtils.fromValueNode(valueNode);

        FieldVisitor fieldVisitor = visitor.visitField(access, name, descriptor, signature, value);

        // visitAnnotation/visitTypeAnnotation
        visitAnnotations(fieldVisitor);

        // visitEnd
        fieldVisitor.visitEnd();
    }
}
