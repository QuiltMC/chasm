package org.quiltmc.chasm.transformer.field;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.Opcodes;
import org.quiltmc.chasm.NodeConstants;
import org.quiltmc.chasm.transformer.SliceTarget;
import org.quiltmc.chasm.transformer.Transformation;
import org.quiltmc.chasm.transformer.Transformer;
import org.quiltmc.chasm.tree.LinkedHashMapNode;
import org.quiltmc.chasm.tree.LinkedListNode;
import org.quiltmc.chasm.tree.ListNode;
import org.quiltmc.chasm.tree.MapNode;
import org.quiltmc.chasm.tree.Node;
import org.quiltmc.chasm.tree.ValueNode;

public class AddField implements Transformer {
    @Override
    public Collection<Transformation> apply(ListNode classes) {
        MapNode newFieldNode = new LinkedHashMapNode();
        newFieldNode.put(NodeConstants.ACCESS, new ValueNode<>(Opcodes.ACC_PUBLIC));
        newFieldNode.put(NodeConstants.NAME, new ValueNode<>("field1"));
        newFieldNode.put(NodeConstants.DESCRIPTOR, new ValueNode<>("I"));
        newFieldNode.put(NodeConstants.SIGNATURE, new ValueNode<>(null));
        newFieldNode.put(NodeConstants.VALUE, new ValueNode<>(null));
        newFieldNode.put(NodeConstants.ANNOTATIONS, new LinkedListNode());
        newFieldNode.put(NodeConstants.ATTRIBUTES, new LinkedListNode());

        ListNode newFields = new LinkedListNode();
        newFields.add(newFieldNode);

        List<Transformation> transformations = new ArrayList<>();
        for (Node node : classes) {
            MapNode classNode = (MapNode) node;
            ListNode fieldsNode = (ListNode) classNode.get(NodeConstants.FIELDS);
            SliceTarget sliceTarget = new SliceTarget(fieldsNode.getPath(), 0, 0);
            transformations.add(new Transformation(this, sliceTarget, Map.of(), (target, sources) -> newFields));
        }

        return transformations;
    }

    @Override
    public String getId() {
        return AddField.class.getCanonicalName();
    }
}
