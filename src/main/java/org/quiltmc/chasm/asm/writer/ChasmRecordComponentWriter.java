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
        ListNode attributesListNode = (ListNode) componentNode.get(NodeConstants.ATTRIBUTES);
        if (attributesListNode == null) {
            return;
        }
        for (Node n : attributesListNode) {
            componentVisitor.visitAttribute(((ValueNode<Attribute>) n).getValue());
        }
    }

    private void visitAnnotations(RecordComponentVisitor componentVisitor) {
        ListNode annotationsListNode = (ListNode) componentNode.get(NodeConstants.ANNOTATIONS);
        if (annotationsListNode == null) {
            return;
        }
        for (Node n : annotationsListNode) {
            ChasmAnnotationWriter writer = new ChasmAnnotationWriter(n);
            writer.visitAnnotation(componentVisitor::visitAnnotation, componentVisitor::visitTypeAnnotation);
        }
    }

    public void visitRecordComponent(ClassVisitor visitor) {
        String name = ((ValueNode<String>) componentNode.get(NodeConstants.NAME)).getValue();
        String descriptor = ((ValueNode<String>) componentNode.get(NodeConstants.DESCRIPTOR)).getValue();
        
        ValueNode<String> signatureNode = (ValueNode<String>) componentNode.get(NodeConstants.SIGNATURE);
        String signature = signatureNode == null? null: signatureNode.getValue();

        RecordComponentVisitor componentVisitor = visitor.visitRecordComponent(name, descriptor, signature);

        // visitAnnotation/visitTypeAnnotation
        visitAnnotations(componentVisitor);

        // visitAttribute
        visitAttributes(componentVisitor);

        // visitEnd
        componentVisitor.visitEnd();
    }
}
