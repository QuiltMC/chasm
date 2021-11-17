package org.quiltmc.chasm.asm;

import org.objectweb.asm.*;
import org.quiltmc.chasm.tree.*;

public class ChasmRecordComponentVisitor extends RecordComponentVisitor {
    private final ListNode annotations = new LinkedListNode();
    private final ListNode attributes = new LinkedListNode();

    public ChasmRecordComponentVisitor(int api, MapNode recordComponentNode) {
        super(api);

        recordComponentNode.put("annotations", annotations);
        recordComponentNode.put("attributes", attributes);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        MapNode annotation = new LinkedHashMapNode();
        ListNode values = new LinkedListNode();
        annotation.put("descriptor", new ValueNode<>(descriptor));
        annotation.put("visible", new ValueNode<>(visible));
        annotation.put("values", new ValueNode<>(values));
        annotations.add(annotation);

        return new ChasmAnnotationVisitor(api, values);
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        MapNode annotation = new LinkedHashMapNode();
        ListNode values = new LinkedListNode();
        annotation.put("descriptor", new ValueNode<>(descriptor));
        annotation.put("visible", new ValueNode<>(visible));
        annotation.put("values", new ValueNode<>(visible));
        annotation.put("typeRef", new ValueNode<>(typeRef));
        annotation.put("typePath", new ValueNode<>(typePath.toString()));
        annotations.add(annotation);

        return new ChasmAnnotationVisitor(api, values);
    }

    @Override
    public void visitAttribute(Attribute attribute) {
        attributes.add(new ValueNode<>(attribute));
    }

    @Override
    public void visitEnd() {
        // Nothing to do here
    }
}
