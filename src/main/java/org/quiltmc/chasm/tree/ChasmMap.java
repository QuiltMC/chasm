package org.quiltmc.chasm.tree;

import java.util.HashMap;

public class ChasmMap extends HashMap<String, ChasmNode> implements ChasmNode {
    @Override
    public ChasmMap evaluate(EvaluationContext context) {
        ChasmMap evaluated = new ChasmMap();
        for (var entry : this.entrySet()) {
            evaluated.put(entry.getKey(), entry.getValue().evaluate(context));
        }
        return evaluated;
    }
}
