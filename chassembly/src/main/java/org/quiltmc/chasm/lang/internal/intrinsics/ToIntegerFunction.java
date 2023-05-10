package org.quiltmc.chasm.lang.internal.intrinsics;

import org.quiltmc.chasm.lang.api.ast.Ast;
import org.quiltmc.chasm.lang.api.ast.FloatNode;
import org.quiltmc.chasm.lang.api.ast.IntegerNode;
import org.quiltmc.chasm.lang.api.ast.Node;
import org.quiltmc.chasm.lang.api.ast.StringNode;
import org.quiltmc.chasm.lang.api.eval.Evaluator;
import org.quiltmc.chasm.lang.api.eval.IntrinsicFunction;
import org.quiltmc.chasm.lang.api.eval.SourceSpan;
import org.quiltmc.chasm.lang.api.exception.EvaluationException;

public class ToIntegerFunction extends IntrinsicFunction {
    @Override
    public Node apply(Evaluator evaluator, Node arg) {
        if (arg instanceof IntegerNode) {
            return arg;
        } else if (arg instanceof FloatNode) {
            return Ast.literal(((FloatNode) arg).getValue().longValue());
        } else if (arg instanceof StringNode) {
            String str = ((StringNode) arg).getValue();
            try {
                return Ast.literal(Long.parseLong(str));
            } catch (NumberFormatException e) {
                throw new EvaluationException(
                        "Cannot convert string \"" + str + "\" to integer",
                        arg.getMetadata().get(SourceSpan.class)
                );
            }
        } else {
            throw new EvaluationException(
                    "Cannot convert " + arg.typeName() + " to integer",
                    arg.getMetadata().get(SourceSpan.class)
            );
        }
    }

    @Override
    public String getName() {
        return "to_integer";
    }
}
