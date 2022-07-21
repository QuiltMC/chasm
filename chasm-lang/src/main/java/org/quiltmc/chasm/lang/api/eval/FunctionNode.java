package org.quiltmc.chasm.lang.api.eval;

import org.quiltmc.chasm.lang.api.ast.Node;

public abstract class FunctionNode extends Node {
    public abstract Node apply(Evaluator evaluator, Node arg);

    @Override
    public final void resolve(Resolver resolver) {
    }

    @Override
    public final Node evaluate(Evaluator evaluator) {
        return this;
    }
}
