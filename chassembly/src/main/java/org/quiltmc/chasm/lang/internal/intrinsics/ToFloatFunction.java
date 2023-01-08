package org.quiltmc.chasm.lang.internal.intrinsics;

import org.quiltmc.chasm.lang.api.ast.FloatNode;
import org.quiltmc.chasm.lang.api.ast.IntegerNode;
import org.quiltmc.chasm.lang.api.ast.Node;
import org.quiltmc.chasm.lang.api.ast.StringNode;
import org.quiltmc.chasm.lang.api.eval.Evaluator;
import org.quiltmc.chasm.lang.api.eval.IntrinsicFunction;
import org.quiltmc.chasm.lang.api.eval.SourceSpan;
import org.quiltmc.chasm.lang.api.exception.EvaluationException;

public class ToFloatFunction extends IntrinsicFunction {
    @Override
    public Node apply(Evaluator evaluator, Node arg) {
        if (arg instanceof FloatNode) {
            return arg;
        } else if (arg instanceof IntegerNode) {
            return new FloatNode(((IntegerNode) arg).getValue().doubleValue());
        } else if (arg instanceof StringNode) {
            String str = ((StringNode) arg).getValue();
            try {
                return new FloatNode(Double.parseDouble(str));
            } catch (NumberFormatException e) {
                throw new EvaluationException(
                        "Cannot convert string \"" + str + "\" to float",
                        arg.getMetadata().get(SourceSpan.class)
                );
            }
        } else {
            throw new EvaluationException(
                    "Cannot convert " + arg + " to float",
                    arg.getMetadata().get(SourceSpan.class)
            );
        }
    }

    @Override
    public String getName() {
        return "to_float";
    }
}
