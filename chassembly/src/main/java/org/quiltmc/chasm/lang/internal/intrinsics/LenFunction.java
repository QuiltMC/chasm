package org.quiltmc.chasm.lang.internal.intrinsics;

import org.quiltmc.chasm.lang.api.ast.Ast;
import org.quiltmc.chasm.lang.api.ast.ListNode;
import org.quiltmc.chasm.lang.api.ast.Node;
import org.quiltmc.chasm.lang.api.eval.Evaluator;
import org.quiltmc.chasm.lang.api.eval.IntrinsicFunction;
import org.quiltmc.chasm.lang.api.eval.SourceSpan;
import org.quiltmc.chasm.lang.api.exception.EvaluationException;

public class LenFunction extends IntrinsicFunction {
    @Override
    public Node apply(Evaluator evaluator, Node arg) {
        if (!(arg instanceof ListNode)) {
            throw new EvaluationException(
                    "Built-in function \"len\" can only be applied to lists but found " + arg.typeName(),
                    arg.getMetadata().get(SourceSpan.class)
            );
        }
        return Ast.literal(((ListNode) arg).size());
    }

    @Override
    public String getName() {
        return "len";
    }
}
