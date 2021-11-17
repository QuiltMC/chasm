package org.quiltmc.chasm.transformer;

import org.quiltmc.chasm.tree.MapNode;
import org.quiltmc.chasm.tree.Node;

import java.util.Map;

public abstract class Transformation {
    private final Transformer parent;
    private final Target target;
    private final Map<String, Target> sources;

    public Transformation(Transformer parent, Target target, Map<String, Target> sources) {
        this.parent = parent;
        this.target = target;
        this.sources = sources;
    }

    public abstract Node apply(Node target, MapNode sources);

    public final Target getTarget() {
        return target;
    }

    public final Map<String, Target> getSources() {
        return sources;
    }

    public Transformer getParent() {
        return parent;
    }
}
