package org.quiltmc.chasm.internal.tree.reader;

import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.RecordComponentVisitor;
import org.quiltmc.chasm.api.tree.ListNode;
import org.quiltmc.chasm.api.tree.MapNode;
import org.quiltmc.chasm.api.tree.Node;
import org.quiltmc.chasm.api.tree.ValueNode;
import org.quiltmc.chasm.internal.util.NodeConstants;

public class RecordComponentNodeReader {
    private final MapNode componentNode;

    public RecordComponentNodeReader(MapNode componentNode) {
        this.componentNode = componentNode;
    }

    private void visitAttributes(RecordComponentVisitor componentVisitor) {
        ListNode attributesListNode = Node.asList(componentNode.get(NodeConstants.ATTRIBUTES));
        if (attributesListNode == null) {
            return;
        }
        for (Node n : attributesListNode) {
            componentVisitor.visitAttribute(Node.asValue(n).getValueAs(Attribute.class));
        }
    }

    private void visitAnnotations(RecordComponentVisitor componentVisitor) {
        ListNode annotationsListNode = Node.asList(componentNode.get(NodeConstants.ANNOTATIONS));
        if (annotationsListNode == null) {
            return;
        }
        for (Node n : annotationsListNode) {
            AnnotationNodeReader writer = new AnnotationNodeReader(n);
            writer.visitAnnotation(componentVisitor::visitAnnotation, componentVisitor::visitTypeAnnotation);
        }
    }

    public void visitRecordComponent(ClassVisitor visitor) {
        String name = Node.asValue(componentNode.get(NodeConstants.NAME)).getValueAsString();
        String descriptor = Node.asValue(componentNode.get(NodeConstants.DESCRIPTOR)).getValueAsString();

        ValueNode signatureNode = Node.asValue(componentNode.get(NodeConstants.SIGNATURE));
        String signature = signatureNode == null ? null : signatureNode.getValueAsString();

        RecordComponentVisitor componentVisitor = visitor.visitRecordComponent(name, descriptor, signature);

        // visitAnnotation/visitTypeAnnotation
        visitAnnotations(componentVisitor);

        // visitAttribute
        visitAttributes(componentVisitor);

        // visitEnd
        componentVisitor.visitEnd();
    }
}
