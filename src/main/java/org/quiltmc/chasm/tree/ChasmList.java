package org.quiltmc.chasm.tree;

import java.util.Collection;
import java.util.LinkedList;

public class ChasmList extends LinkedList<ChasmNode> implements ChasmNode {
    public ChasmList() {
        super();
    }

    public ChasmList(Collection<? extends ChasmNode> nodes) {
        super(nodes);
    }

    @Override
    public ChasmList evaluate(EvaluationContext context) {
        ChasmList evaluated = new ChasmList();
        for (var entry : this) {
            evaluated.add(entry.evaluate(context));
        }
        return evaluated;
    }
}
