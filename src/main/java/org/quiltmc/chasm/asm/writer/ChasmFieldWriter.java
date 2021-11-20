package org.quiltmc.chasm.asm.writer;

import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.quiltmc.chasm.NodeConstants;
import org.quiltmc.chasm.tree.ListNode;
import org.quiltmc.chasm.tree.MapNode;
import org.quiltmc.chasm.tree.Node;
import org.quiltmc.chasm.tree.ValueNode;

@SuppressWarnings("unchecked")
public class ChasmFieldWriter {
    private final MapNode fieldNode;

    public ChasmFieldWriter(MapNode fieldNode) {
        this.fieldNode = fieldNode;
    }

    private void visitAttributes(FieldVisitor fieldVisitor) {
        for (Node n : (ListNode) fieldNode.get(NodeConstants.ATTRIBUTES)) {
            fieldVisitor.visitAttribute(((ValueNode<Attribute>) n).getValue());
        }
    }

    private void visitAnnotations(FieldVisitor fieldVisitor) {
        for (Node n : (ListNode) fieldNode.get(NodeConstants.ANNOTATIONS)) {
            ChasmAnnotationWriter annotationWriter = new ChasmAnnotationWriter((MapNode) n);
            annotationWriter.visitAnnotation(fieldVisitor::visitAnnotation, fieldVisitor::visitTypeAnnotation);
        }
    }

    public void visitField(ClassVisitor visitor) {
        int access = fieldNode.get(NodeConstants.ACCESS).getAsInt();
        String name = fieldNode.get(NodeConstants.NAME).getAsString();
        String descriptor = fieldNode.get(NodeConstants.DESCRIPTOR).getAsString();
        String signature = fieldNode.get(NodeConstants.SIGNATURE).getAsString();
        Object value = fieldNode.get(NodeConstants.VALUE).getAsObject();

        FieldVisitor fieldVisitor = visitor.visitField(access, name, descriptor, signature, value);

        // visitAnnotation/visitTypeAnnotation
        visitAnnotations(fieldVisitor);

        // visitAttribute
        visitAttributes(fieldVisitor);

        // visitEnd
        fieldVisitor.visitEnd();
    }
}
