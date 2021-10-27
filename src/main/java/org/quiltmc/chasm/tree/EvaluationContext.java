package org.quiltmc.chasm.tree;

import java.util.HashMap;
import java.util.Map;

public class EvaluationContext {
    private final Map<String, ChasmNode> defined = new HashMap<>();

    public ChasmNode resolveReference(String identifier) {
        return defined.get(identifier);
    }

    public EvaluationContext with(String identifier, ChasmNode value) {
        EvaluationContext context = new EvaluationContext();
        context.defined.putAll(this.defined);
        context.defined.put(identifier, value);
        return context;
    }
}
