package org.quiltmc.chasm.internal.transformer.tree;

import org.antlr.v4.runtime.tree.ParseTree;
import org.quiltmc.chasm.api.tree.ListNode;
import org.quiltmc.chasm.api.tree.MapNode;
import org.quiltmc.chasm.api.tree.Node;
import org.quiltmc.chasm.api.tree.ValueNode;
import org.quiltmc.chasm.lang.op.Expression;

public interface NodeExpression extends Expression {
    Node getNode();

    static Expression from(ParseTree tree, Node node) {
        if (node instanceof MapNode) {
            return new MapNodeExpression(tree, (MapNode) node);
        } else if (node instanceof ListNode) {
            return new ListNodeExpression(tree, (ListNode) node);
        } else if (node instanceof ValueNode) {
            ValueNode valueNode = (ValueNode) node;
            Object value = valueNode.getValue();
            if (value instanceof Integer) {
                return new IntegerNodeExpression(tree, valueNode);
            } else if (value instanceof Boolean) {
                return new BooleanNodeExpression(tree, valueNode);
            } else if (value instanceof String) {
                return new StringNodeExpression(tree, valueNode);
            } else {
                return new ObjectNodeExpression(tree, valueNode);
            }
        } else {
            throw new RuntimeException("Unknown chasm node type: " + node.getClass());
        }
    }
}
