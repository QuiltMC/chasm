package org.quiltmc.chasm.internal.tree.reader;

import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.quiltmc.chasm.api.tree.ListNode;
import org.quiltmc.chasm.api.tree.MapNode;
import org.quiltmc.chasm.api.tree.Node;
import org.quiltmc.chasm.api.tree.ValueNode;
import org.quiltmc.chasm.internal.util.NodeConstants;

@SuppressWarnings("unchecked")
public class FieldNodeReader {
    private final MapNode<Node> fieldNode;

    public FieldNodeReader(MapNode<Node> fieldNode) {
        this.fieldNode = fieldNode;
    }

    private void visitAttributes(FieldVisitor fieldVisitor) {
        ListNode<Node> attributesListNode = (ListNode<Node>) fieldNode.get(NodeConstants.ATTRIBUTES);
        if (attributesListNode == null) {
            return;
        }
        for (Node n : attributesListNode) {
            fieldVisitor.visitAttribute(((ValueNode<Attribute>) n).getValue());
        }
    }

    private void visitAnnotations(FieldVisitor fieldVisitor) {
        ListNode<Node> annotationsListNode = (ListNode<Node>) fieldNode.get(NodeConstants.ANNOTATIONS);
        if (annotationsListNode == null) {
            return;
        }
        for (Node n : annotationsListNode) {
            AnnotationNodeReader annotationWriter = new AnnotationNodeReader(n);
            annotationWriter.visitAnnotation(fieldVisitor::visitAnnotation, fieldVisitor::visitTypeAnnotation);
        }
    }

    public void visitField(ClassVisitor visitor) {
        int access = ((ValueNode<Integer>) fieldNode.get(NodeConstants.ACCESS)).getValue();
        String name = ((ValueNode<String>) fieldNode.get(NodeConstants.NAME)).getValue();
        String descriptor = ((ValueNode<String>) fieldNode.get(NodeConstants.DESCRIPTOR)).getValue();

        ValueNode<String> signatureNode = (ValueNode<String>) fieldNode.get(NodeConstants.SIGNATURE);
        String signature = signatureNode == null ? null : signatureNode.getValue();

        ValueNode<Object> valueNode = (ValueNode<Object>) fieldNode.get(NodeConstants.VALUE);
        Object value = valueNode == null ? null : valueNode.getValue();

        FieldVisitor fieldVisitor = visitor.visitField(access, name, descriptor, signature, value);

        // visitAnnotation/visitTypeAnnotation
        visitAnnotations(fieldVisitor);

        // visitAttribute
        visitAttributes(fieldVisitor);

        // visitEnd
        fieldVisitor.visitEnd();
    }
}
