package org.quiltmc.chasm.asm.writer;

import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.RecordComponentVisitor;
import org.quiltmc.chasm.NodeConstants;
import org.quiltmc.chasm.tree.ListNode;
import org.quiltmc.chasm.tree.MapNode;
import org.quiltmc.chasm.tree.Node;
import org.quiltmc.chasm.tree.ValueNode;

@SuppressWarnings("unchecked")
public class ChasmRecordComponentWriter {
    private final MapNode componentNode;

    public ChasmRecordComponentWriter(MapNode componentNode) {
        this.componentNode = componentNode;
    }

    private void visitAttributes(RecordComponentVisitor componentVisitor) {
        for (Node n : (ListNode) componentNode.get(NodeConstants.ATTRIBUTES)) {
            componentVisitor.visitAttribute(((ValueNode<Attribute>) n).getValue());
        }
    }

    private void visitAnnotations(RecordComponentVisitor componentVisitor) {
        for (Node n : (ListNode) componentNode.get(NodeConstants.ANNOTATIONS)) {
            ChasmAnnotationWriter writer = new ChasmAnnotationWriter((MapNode) n);
            writer.visitAnnotation(componentVisitor::visitAnnotation, componentVisitor::visitTypeAnnotation);
        }
    }

    public void visitRecordComponent(ClassVisitor visitor) {
        String name = ((ValueNode<String>) componentNode.get(NodeConstants.NAME)).getValue();
        String descriptor = ((ValueNode<String>) componentNode.get(NodeConstants.DESCRIPTOR)).getValue();
        
        ValueNode<String> signatureNode = (ValueNode<String>) componentNode.get(NodeConstants.SIGNATURE);
        String signature = signatureNode == null? descriptor: signatureNode.getValue();

        RecordComponentVisitor componentVisitor = visitor.visitRecordComponent(name, descriptor, signature);

        // visitAnnotation/visitTypeAnnotation
        visitAnnotations(componentVisitor);

        // visitAttribute
        visitAttributes(componentVisitor);

        // visitEnd
        componentVisitor.visitEnd();
    }
}
