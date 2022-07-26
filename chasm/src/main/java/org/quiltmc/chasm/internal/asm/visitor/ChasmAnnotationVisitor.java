package org.quiltmc.chasm.internal.asm.visitor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.objectweb.asm.AnnotationVisitor;
import org.quiltmc.chasm.internal.util.NodeConstants;
import org.quiltmc.chasm.internal.util.NodeUtils;
import org.quiltmc.chasm.lang.api.ast.ListNode;
import org.quiltmc.chasm.lang.api.ast.MapNode;
import org.quiltmc.chasm.lang.api.ast.Node;
import org.quiltmc.chasm.lang.api.ast.StringNode;

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
        MapNode enumValueNode = new MapNode(new LinkedHashMap<>());
        enumValueNode.getEntries().put(NodeConstants.DESCRIPTOR, new StringNode(descriptor));
        enumValueNode.getEntries().put(NodeConstants.VALUE, new StringNode(value));

        visitValueNode(name, enumValueNode);

        super.visitEnum(name, descriptor, value);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String name, String descriptor) {
        MapNode annotationValueNode = new MapNode(new LinkedHashMap<>());
        MapNode values = new MapNode(new LinkedHashMap<>());
        annotationValueNode.getEntries().put(NodeConstants.DESCRIPTOR, new StringNode(descriptor));
        annotationValueNode.getEntries().put(NodeConstants.VALUES, values);

        visitValueNode(name, annotationValueNode);

        return new ChasmAnnotationVisitor(api, values, super.visitAnnotation(name, descriptor));
    }

    @Override
    public AnnotationVisitor visitArray(String name) {
        ListNode values = new ListNode(new ArrayList<>());

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

            NodeUtils.asMap(values).getEntries().put(name, NodeUtils.getValueNode(value));
        }

        // If this is processing a list, name must not be set
        if (values instanceof ListNode) {
            NodeUtils.asList(values).getEntries().add(NodeUtils.getValueNode(value));
        }
    }
}
