package org.quiltmc.chasm.lang.api.eval;

import org.quiltmc.chasm.lang.api.ast.Node;

/**
 * The base class for any node that can be applied using a call expression (a "function").
 */
public abstract class FunctionNode extends Node {
    /**
     * Applies the function using the given argument.
     */
    public abstract Node apply(Evaluator evaluator, Node arg);

    @Override
    public final void resolve(Resolver resolver) {
    }

    @Override
    public final Node evaluate(Evaluator evaluator) {
        return this;
    }

    @Override
    public String typeName() {
        return "function";
    }
}
