package org.quiltmc.chasm.lang.interop;

import java.util.Map;

import org.quiltmc.chasm.api.tree.ArrayListNode;
import org.quiltmc.chasm.api.tree.LinkedHashMapNode;
import org.quiltmc.chasm.api.tree.ListNode;
import org.quiltmc.chasm.api.tree.MapNode;
import org.quiltmc.chasm.api.tree.Node;
import org.quiltmc.chasm.api.tree.ValueNode;
import org.quiltmc.chasm.lang.ast.BooleanExpression;
import org.quiltmc.chasm.lang.ast.Expression;
import org.quiltmc.chasm.lang.ast.ListExpression;
import org.quiltmc.chasm.lang.ast.LiteralExpression;
import org.quiltmc.chasm.lang.ast.MapExpression;

public abstract class ConversionHelper {
    private ConversionHelper() {
    }

    public static Node convert(Expression expression) {
        if (expression instanceof MapExpression) {
            MapNode mapNode = new LinkedHashMapNode();
            for (Map.Entry<String, Expression> entry : ((MapExpression) expression).getEntries().entrySet()) {
                mapNode.put(entry.getKey(), convert(entry.getValue()));
            }
            return mapNode;
        }

        if (expression instanceof ListExpression) {
            ListNode mapNode = new ArrayListNode();
            for (Expression entry : ((ListExpression) expression).getEntries()) {
                mapNode.add(convert(entry));
            }
            return mapNode;
        }

        if (expression instanceof LiteralExpression<?>) {
            return new ValueNode<>(((LiteralExpression<?>) expression).getValue());
        }

        throw new RuntimeException("Can't convert Expression to Node.");
    }

    public static Expression convert(Node node) {
        if (node instanceof MapNode) {
            return new ChasmMapNodeExpression((MapNode) node);
        }
        if (node instanceof ListNode) {
            return new ChasmListNodeExpression((ListNode) node);
        }
        if (node instanceof ValueNode<?>) {
            Object value = ((ValueNode<?>) node).getValue();
            if (value instanceof String) {
                //noinspection unchecked
                return new ChasmStringNodeExpression((ValueNode<String>) node);
            }
            if (value instanceof Integer) {
                //noinspection unchecked
                return new ChasmIntegerNodeExpression((ValueNode<Integer>) node);
            }
            if (value instanceof BooleanExpression) {
                //noinspection unchecked
                return new ChasmBooleanNodeExpression((ValueNode<Boolean>) node);
            }
        }

        throw new RuntimeException("Can't convert node to expression.");
    }
}
