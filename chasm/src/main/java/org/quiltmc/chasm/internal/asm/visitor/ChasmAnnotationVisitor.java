package org.quiltmc.chasm.internal.asm.visitor;

import org.objectweb.asm.AnnotationVisitor;
import org.quiltmc.chasm.internal.util.NodeConstants;
import org.quiltmc.chasm.internal.util.NodeUtils;
import org.quiltmc.chasm.lang.api.ast.Ast;
import org.quiltmc.chasm.lang.api.ast.ListNode;
import org.quiltmc.chasm.lang.api.ast.MapNode;
import org.quiltmc.chasm.lang.api.ast.Node;

public class ChasmAnnotationVisitor extends AnnotationVisitor {
    private final Node values;

    public ChasmAnnotationVisitor(int api, Node values) {
        this(api, values, null);
    }

    public ChasmAnnotationVisitor(int api, Node values, AnnotationVisitor av) {
        super(api, av);

        this.values = values;
    }

    @Override
    public void visit(String name, Object value) {
        if (value instanceof Object[]) {
            // Process Object[] using visitor pattern
            AnnotationVisitor visitor = visitArray(name);
            for (Object entry : (Object[]) value) {
                visitor.visit(null, entry);
            }
            visitor.visitEnd();
            return;
        }

        visitValueNode(name, NodeUtils.getValueNode(value));

        super.visit(name, value);
    }


    @Override
    public void visitEnum(String name, String descriptor, String value) {
        MapNode enumValueNode = Ast.map()
                .put(NodeConstants.DESCRIPTOR, descriptor)
                .put(NodeConstants.VALUE, value)
                .build();

        visitValueNode(name, enumValueNode);

        super.visitEnum(name, descriptor, value);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String name, String descriptor) {
        MapNode values = Ast.emptyMap();
        MapNode annotationValueNode = Ast.map()
                .put(NodeConstants.DESCRIPTOR, descriptor)
                .put(NodeConstants.VALUES, values)
                .build();

        visitValueNode(name, annotationValueNode);

        return new ChasmAnnotationVisitor(api, values, super.visitAnnotation(name, descriptor));
    }

    @Override
    public AnnotationVisitor visitArray(String name) {
        ListNode values = Ast.emptyList();

        visitValueNode(name, values);

        return new ChasmAnnotationVisitor(api, values, super.visitArray(name));
    }

    @Override
    public void visitEnd() {
        // Nothing to do
        super.visitEnd();
    }

    private void visitValueNode(String name, Node value) {
        // If this is processing a map, name must be set
        if (values instanceof MapNode) {
            if (name == null) {
                throw new RuntimeException("Annotation value is missing name");
            }

            NodeUtils.asMap(values).put(name, value);
        }

        // If this is processing a list, name must not be set
        if (values instanceof ListNode) {
            NodeUtils.asList(values).add(value);
        }
    }
}
