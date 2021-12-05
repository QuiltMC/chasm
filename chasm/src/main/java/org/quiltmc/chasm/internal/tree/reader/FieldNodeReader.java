package org.quiltmc.chasm.internal.tree.reader;

import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.quiltmc.chasm.api.tree.ListNode;
import org.quiltmc.chasm.api.tree.MapNode;
import org.quiltmc.chasm.api.tree.Node;
import org.quiltmc.chasm.api.tree.ValueNode;
import org.quiltmc.chasm.internal.util.NodeConstants;

public class FieldNodeReader {
    private final MapNode fieldNode;

    public FieldNodeReader(MapNode fieldNode) {
        this.fieldNode = fieldNode;
    }

    private void visitAttributes(FieldVisitor fieldVisitor) {
        ListNode attributesListNode = Node.asList(fieldNode.get(NodeConstants.ATTRIBUTES));
        if (attributesListNode == null) {
            return;
        }
        for (Node n : attributesListNode) {
            fieldVisitor.visitAttribute(Node.asValue(n).getValueAs(Attribute.class));
        }
    }

    private void visitAnnotations(FieldVisitor fieldVisitor) {
        ListNode annotationsListNode = Node.asList(fieldNode.get(NodeConstants.ANNOTATIONS));
        if (annotationsListNode == null) {
            return;
        }
        for (Node n : annotationsListNode) {
            AnnotationNodeReader annotationWriter = new AnnotationNodeReader(n);
            annotationWriter.visitAnnotation(fieldVisitor::visitAnnotation, fieldVisitor::visitTypeAnnotation);
        }
    }

    public void visitField(ClassVisitor visitor) {
        int access = Node.asValue(fieldNode.get(NodeConstants.ACCESS)).getValueAsInt();
        String name = Node.asValue(fieldNode.get(NodeConstants.NAME)).getValueAsString();
        String descriptor = Node.asValue(fieldNode.get(NodeConstants.DESCRIPTOR)).getValueAsString();

        ValueNode signatureNode = Node.asValue(fieldNode.get(NodeConstants.SIGNATURE));
        String signature = signatureNode == null ? null : signatureNode.getValueAsString();

        ValueNode valueNode = Node.asValue(fieldNode.get(NodeConstants.VALUE));
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
