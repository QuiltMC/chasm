package org.quiltmc.chasm.transformer.field;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.Opcodes;
import org.quiltmc.chasm.api.Transformation;
import org.quiltmc.chasm.api.Transformer;
import org.quiltmc.chasm.api.target.SliceTarget;
import org.quiltmc.chasm.api.target.Target;
import org.quiltmc.chasm.api.tree.ArrayListNode;
import org.quiltmc.chasm.api.tree.LinkedHashMapNode;
import org.quiltmc.chasm.api.tree.ListNode;
import org.quiltmc.chasm.api.tree.MapNode;
import org.quiltmc.chasm.api.tree.Node;
import org.quiltmc.chasm.api.tree.WrapperValueNode;
import org.quiltmc.chasm.internal.util.NodeConstants;

public class AddField implements Transformer {
    @Override
    public Collection<Transformation> apply(ListNode classes) {
        MapNode newFieldNode = new LinkedHashMapNode();
        newFieldNode.put(NodeConstants.ACCESS, new WrapperValueNode(Opcodes.ACC_PUBLIC));
        newFieldNode.put(NodeConstants.NAME, new WrapperValueNode("field1"));
        newFieldNode.put(NodeConstants.DESCRIPTOR, new WrapperValueNode("I"));
        newFieldNode.put(NodeConstants.SIGNATURE, new WrapperValueNode(null));
        newFieldNode.put(NodeConstants.VALUE, new WrapperValueNode(null));
        newFieldNode.put(NodeConstants.ANNOTATIONS, new ArrayListNode());
        newFieldNode.put(NodeConstants.ATTRIBUTES, new ArrayListNode());

        ListNode newFields = new ArrayListNode();
        newFields.add(newFieldNode);

        List<Transformation> transformations = new ArrayList<>();
        for (Node node : classes) {
            MapNode classNode = Node.asMap(node);
            ListNode fieldsNode = Node.asList(classNode.get(NodeConstants.FIELDS));
            SliceTarget sliceTarget = new SliceTarget(fieldsNode, 0, 0);
            transformations.add(new StaticTransformation(sliceTarget, newFields));
        }

        return transformations;
    }

    @Override
    public String getId() {
        return AddField.class.getCanonicalName();
    }

    private class StaticTransformation implements Transformation {
        private final Target target;
        private final Node replacement;

        public StaticTransformation(Target target, Node replacement) {
            this.target = target;
            this.replacement = replacement;
        }

        @Override
        public Transformer getParent() {
            return AddField.this;
        }

        @Override
        public Target getTarget() {
            return target;
        }

        @Override
        public Node apply(Node targetNode, Map<String, Node> nodeSources) {
            return replacement;
        }
    }
}
