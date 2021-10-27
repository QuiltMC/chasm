package org.quiltmc.chasm.tree;

public class ChasmFunction implements ChasmNode {
    private final String parameterName;
    private final ChasmNode body;

    public ChasmFunction(String parameterName, ChasmNode body) {
        this.parameterName = parameterName;
        this.body = body;
    }

    @Override
    public ChasmFunction evaluate(EvaluationContext context) {
        return this;
    }

    public String getParameterName() {
        return parameterName;
    }

    public ChasmNode getBody() {
        return body;
    }
}
