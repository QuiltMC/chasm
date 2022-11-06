package org.quiltmc.chasm.lang.internal.intrinsics;

import org.quiltmc.chasm.lang.api.ast.FloatNode;
import org.quiltmc.chasm.lang.api.ast.IntegerNode;
import org.quiltmc.chasm.lang.api.ast.Node;
import org.quiltmc.chasm.lang.api.ast.StringNode;
import org.quiltmc.chasm.lang.api.eval.Evaluator;
import org.quiltmc.chasm.lang.api.eval.IntrinsicFunction;
import org.quiltmc.chasm.lang.api.exception.EvaluationException;

public class ToIntegerFunction extends IntrinsicFunction {
    @Override
    public Node apply(Evaluator evaluator, Node arg) {
        if (arg instanceof IntegerNode) {
            return arg;
        } else if (arg instanceof FloatNode) {
            return new IntegerNode(((FloatNode) arg).getValue().longValue());
        } else if (arg instanceof StringNode) {
            String str = ((StringNode) arg).getValue();
            try {
                return new IntegerNode(Long.parseLong(str));
            } catch (NumberFormatException e) {
                throw new EvaluationException("Cannot convert string \"" + str + "\" to integer");
            }
        } else {
            throw new EvaluationException("Cannot convert " + arg + " to integer");
        }
    }

    @Override
    public String getName() {
        return "to_integer";
    }
}
