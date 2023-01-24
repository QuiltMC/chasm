package org.quiltmc.chasm.internal.asm.visitor;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.RecordComponentVisitor;
import org.objectweb.asm.TypePath;
import org.quiltmc.chasm.internal.util.NodeConstants;
import org.quiltmc.chasm.lang.api.ast.Ast;
import org.quiltmc.chasm.lang.api.ast.ListNode;
import org.quiltmc.chasm.lang.api.ast.MapNode;

public class ChasmRecordComponentVisitor extends RecordComponentVisitor {
    private final ListNode annotations = Ast.emptyList();

    public ChasmRecordComponentVisitor(int api, MapNode recordComponentNode) {
        super(api);

        recordComponentNode.put(NodeConstants.ANNOTATIONS, annotations);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        MapNode values = Ast.emptyMap();
        MapNode annotation = Ast.map()
                .put(NodeConstants.DESCRIPTOR, descriptor)
                .put(NodeConstants.VISIBLE, visible)
                .put(NodeConstants.VALUES, values)
                .build();
        annotations.add(annotation);

        return new ChasmAnnotationVisitor(api, values);
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        MapNode values = Ast.emptyMap();
        MapNode annotation = Ast.map()
                .put(NodeConstants.DESCRIPTOR, descriptor)
                .put(NodeConstants.VISIBLE, visible)
                .put(NodeConstants.VALUES, values)
                .put(NodeConstants.TYPE_REF, typeRef)
                .put(NodeConstants.TYPE_PATH, typePath.toString())
                .build();
        annotations.add(annotation);

        return new ChasmAnnotationVisitor(api, values);
    }

    @Override
    public void visitAttribute(Attribute attribute) {
        throw new RuntimeException("Unknown attribute: " + attribute.type);
    }

    @Override
    public void visitEnd() {
        // Nothing to do here
    }
}
