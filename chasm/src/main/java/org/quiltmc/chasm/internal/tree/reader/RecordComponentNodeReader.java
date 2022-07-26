package org.quiltmc.chasm.internal.tree.reader;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.RecordComponentVisitor;
import org.quiltmc.chasm.internal.util.NodeConstants;
import org.quiltmc.chasm.internal.util.NodeUtils;
import org.quiltmc.chasm.lang.api.ast.ListNode;
import org.quiltmc.chasm.lang.api.ast.MapNode;
import org.quiltmc.chasm.lang.api.ast.Node;

public class RecordComponentNodeReader {
    private final MapNode componentNode;

    public RecordComponentNodeReader(MapNode componentNode) {
        this.componentNode = componentNode;
    }

    private void visitAnnotations(RecordComponentVisitor visitor) {
        ListNode annotationsListNode = NodeUtils.getAsList(componentNode, NodeConstants.ANNOTATIONS);
        if (annotationsListNode == null) {
            return;
        }
        for (Node n : annotationsListNode.getEntries()) {
            AnnotationNodeReader reader = new AnnotationNodeReader(n);
            reader.accept(visitor::visitAnnotation, visitor::visitTypeAnnotation);
        }
    }

    public void visitRecordComponent(ClassVisitor visitor) {
        String name = NodeUtils.getAsString(componentNode, NodeConstants.NAME);
        String descriptor = NodeUtils.getAsString(componentNode, NodeConstants.DESCRIPTOR);
        String signature = NodeUtils.getAsString(componentNode, NodeConstants.SIGNATURE);

        RecordComponentVisitor componentVisitor = visitor.visitRecordComponent(name, descriptor, signature);

        // visitAnnotation/visitTypeAnnotation
        visitAnnotations(componentVisitor);

        // visitEnd
        componentVisitor.visitEnd();
    }
}
