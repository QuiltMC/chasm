package org.quiltmc.chasm.lang.internal.intrinsics;

import org.quiltmc.chasm.lang.api.ast.IntegerNode;
import org.quiltmc.chasm.lang.api.ast.ListNode;
import org.quiltmc.chasm.lang.api.ast.MapNode;
import org.quiltmc.chasm.lang.api.ast.Node;
import org.quiltmc.chasm.lang.api.ast.NullNode;
import org.quiltmc.chasm.lang.api.eval.Evaluator;
import org.quiltmc.chasm.lang.api.eval.IntrinsicFunction;
import org.quiltmc.chasm.lang.api.exception.EvaluationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WithElementsFunction extends IntrinsicFunction {
    @Override
    public Node apply(Evaluator evaluator, Node arg) {
        if (!(arg instanceof MapNode)) {
            throw createException(arg);
        }
        Map<String, Node> map = ((MapNode) arg).getEntries();

        Node list = map.get("list");
        if (!(list instanceof ListNode)) {
            throw createException(arg);
        }
        Node elements = map.get("elements");
        if (!(elements instanceof ListNode)) {
            throw createException(arg);
        }

        List<Node> newList = new ArrayList<>(((ListNode) list).getEntries());
        for (Node element : ((ListNode) elements).getEntries()) {
            if (!(element instanceof MapNode)) {
                throw createException(arg);
            }
            Map<String, Node> mapElement = ((MapNode) element).getEntries();

            Node indexNode = mapElement.get("index");
            if (!(indexNode instanceof IntegerNode)) {
                throw createException(arg);
            }
            long index = ((IntegerNode) indexNode).getValue();
            if (index < 0 || index >= newList.size()) {
                throw new EvaluationException("Index " + index + " is out of bounds for size " + newList.size());
            }
            Node value = mapElement.get("value");
            newList.set((int) index, value == null ? new NullNode() : value);
        }

        return new ListNode(newList);
    }

    private static EvaluationException createException(Node arg) {
        return new EvaluationException(
                "Built-in function \"with_elements\" can only be applied to maps, "
                        + "with {list: list, elements: list}, where elements is "
                        + "a list of maps of the form {index: integer: value: any}, "
                        + "but found " + arg
        );
    }

    @Override
    public String getName() {
        return "with_elements";
    }
}
