package org.quiltmc.chasm.lang.internal.intrinsics;

import org.quiltmc.chasm.lang.api.ast.Ast;
import org.quiltmc.chasm.lang.api.ast.ListNode;
import org.quiltmc.chasm.lang.api.ast.MapNode;
import org.quiltmc.chasm.lang.api.ast.Node;
import org.quiltmc.chasm.lang.api.eval.Evaluator;
import org.quiltmc.chasm.lang.api.eval.IntrinsicFunction;
import org.quiltmc.chasm.lang.api.eval.SourceSpan;
import org.quiltmc.chasm.lang.api.exception.EvaluationException;

public class EntriesFunction extends IntrinsicFunction {
    @Override
    public Node apply(Evaluator evaluator, Node arg) {
        if (arg instanceof MapNode) {
            ListNode entryList = Ast.emptyList();

            ((MapNode) arg).getEntries()
                    .forEach((key, value) -> entryList.add(Ast.map().put("key", key).put("value", value).build()));

            return entryList;
        }
        throw new EvaluationException(
                "Built-in function \"entries\" can only be applied to maps but found " + arg,
                arg.getMetadata().get(SourceSpan.class)
        );
    }

    @Override
    public String getName() {
        return "entries";
    }
}
