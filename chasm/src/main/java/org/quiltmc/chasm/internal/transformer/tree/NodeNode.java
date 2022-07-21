package org.quiltmc.chasm.internal.transformer.tree;

import org.antlr.v4.runtime.tree.ParseTree;
import org.quiltmc.chasm.api.tree.ListNode;
import org.quiltmc.chasm.api.tree.MapNode;
import org.quiltmc.chasm.api.tree.ValueNode;
import org.quiltmc.chasm.lang.api.ast.Node;

public class NodeNode extends Node {
    org.quiltmc.chasm.api.tree.Node getNode();

    public static Node from(ParseTree tree, org.quiltmc.chasm.api.tree.Node node) {
        if (node instanceof MapNode) {
            return new MapNodeNode(tree, (MapNode) node);
        } else if (node instanceof ListNode) {
            return new ListNodeNode(tree, (ListNode) node);
        } else if (node instanceof ValueNode) {
            ValueNode valueNode = (ValueNode) node;
            Object value = valueNode.getValue();
            if (value instanceof Integer) {
                return new IntegerNodeNode(tree, valueNode);
            } else if (value instanceof Boolean) {
                return new BooleanNodeNode(tree, valueNode);
            } else if (value instanceof String) {
                return new StringNodeNode(tree, valueNode);
            } else {
                return new ObjectNodeNode(tree, valueNode);
            }
        } else {
            throw new RuntimeException("Unknown chasm node type: " + node.getClass());
        }
    }
}
