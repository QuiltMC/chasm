package org.quiltmc.chasm.lang.api.eval;

import org.quiltmc.chasm.lang.api.ast.Node;

public abstract class FunctionNode extends Node {
    public abstract Node apply(Evaluator evaluator, Node node);

    @Override
    public Node copy() {
        return this;
    }

    @Override
    public void resolve(Resolver resolver) {
    }

    @Override
    public Node evaluate(Evaluator evaluator) {
        return this;
    }
}
