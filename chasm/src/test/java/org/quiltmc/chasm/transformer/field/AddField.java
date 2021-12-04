package org.quiltmc.chasm.transformer.field;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.Opcodes;
import org.quiltmc.chasm.api.Transformation;
import org.quiltmc.chasm.api.Transformer;
import org.quiltmc.chasm.api.target.SliceTarget;
import org.quiltmc.chasm.api.tree.ArrayListNode;
import org.quiltmc.chasm.api.tree.FrozenListNode;
import org.quiltmc.chasm.api.tree.FrozenNode;
import org.quiltmc.chasm.api.tree.LinkedHashMapNode;
import org.quiltmc.chasm.api.tree.ListNode;
import org.quiltmc.chasm.api.tree.MapNode;
import org.quiltmc.chasm.api.tree.Node;
import org.quiltmc.chasm.api.tree.ValueNode;
import org.quiltmc.chasm.internal.util.NodeConstants;

public class AddField implements Transformer {
    @SuppressWarnings("unchecked")
    @Override
    public Collection<Transformation> apply(FrozenListNode<? extends FrozenNode> classes) {
        MapNode<Node> newFieldNode = new LinkedHashMapNode();
        newFieldNode.put(NodeConstants.ACCESS, new ValueNode<>(Opcodes.ACC_PUBLIC));
        newFieldNode.put(NodeConstants.NAME, new ValueNode<>("field1"));
        newFieldNode.put(NodeConstants.DESCRIPTOR, new ValueNode<>("I"));
        newFieldNode.put(NodeConstants.SIGNATURE, new ValueNode<>(null));
        newFieldNode.put(NodeConstants.VALUE, new ValueNode<>(null));
        newFieldNode.put(NodeConstants.ANNOTATIONS, new ArrayListNode());
        newFieldNode.put(NodeConstants.ATTRIBUTES, new ArrayListNode());

        ListNode<Node> newFields = new ArrayListNode();
        newFields.add(newFieldNode);
        FrozenListNode<FrozenNode> frozenNewFields = newFields.asImmutable();

        List<Transformation> transformations = new ArrayList<>();
        for (Node node : classes) {
            MapNode<Node> classNode = (MapNode<Node>) node;
            ListNode<Node> fieldsNode = (ListNode<Node>) classNode.get(NodeConstants.FIELDS);
            SliceTarget sliceTarget = new SliceTarget(fieldsNode, 0, 0);
            transformations.add(new Transformation(this, sliceTarget, Map.of(), (target, sources) -> frozenNewFields));
        }

        return transformations;
    }

    @Override
    public String getId() {
        return AddField.class.getCanonicalName();
    }
}
