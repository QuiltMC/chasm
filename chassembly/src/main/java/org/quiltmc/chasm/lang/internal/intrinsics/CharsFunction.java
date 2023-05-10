package org.quiltmc.chasm.lang.internal.intrinsics;

import org.quiltmc.chasm.lang.api.ast.Ast;
import org.quiltmc.chasm.lang.api.ast.ListNode;
import org.quiltmc.chasm.lang.api.ast.Node;
import org.quiltmc.chasm.lang.api.ast.StringNode;
import org.quiltmc.chasm.lang.api.eval.Evaluator;
import org.quiltmc.chasm.lang.api.eval.IntrinsicFunction;
import org.quiltmc.chasm.lang.api.eval.SourceSpan;
import org.quiltmc.chasm.lang.api.exception.EvaluationException;

class CharsFunction extends IntrinsicFunction {
    @Override
    public String getName() {
        return "chars";
    }

    @Override
    public Node apply(Evaluator evaluator, Node arg) {
        if (!(arg instanceof StringNode)) {
            throw new EvaluationException(
                    "Built-in function \"chars\" can only be applied to strings but found " + arg,
                    arg.getMetadata().get(SourceSpan.class)
            );
        }

        String value = ((StringNode) arg).getValue();

        ListNode entries = Ast.emptyList();
        for (int i = 0; i < value.length(); i++) {
            entries.add(Ast.literal(value.charAt(i)));
        }

        return entries;
    }
}
