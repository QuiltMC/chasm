package org.quiltmc.chasm.lang.internal.eval;

import org.quiltmc.chasm.lang.api.ast.LambdaNode;

public class LambdaReference extends Reference {
    private final String identifier;
    private final LambdaNode lambda;

    public LambdaReference(String identifier, LambdaNode lambda) {
        this.identifier = identifier;
        this.lambda = lambda;
    }

    public String getIdentifier() {
        return identifier;
    }

    public LambdaNode getLambda() {
        return lambda;
    }
}
