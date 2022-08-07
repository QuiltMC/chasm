package org.quiltmc.chasm.lang.api.eval;

import org.quiltmc.chasm.lang.api.ast.Node;
import org.quiltmc.chasm.lang.api.ast.BaseNode;

public abstract class FunctionNode extends BaseNode {
    public abstract Node apply(Evaluator evaluator, Node arg);

    @Override
    public final void resolve(Resolver resolver) {
    }

    @Override
    public final Node evaluate(Evaluator evaluator) {
        return this;
    }
}
