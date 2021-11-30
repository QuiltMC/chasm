package org.quiltmc.chasm.api;

import java.util.Map;

import org.quiltmc.chasm.api.target.Target;
import org.quiltmc.chasm.api.tree.Node;

public final class Transformation {
    private final Transformer parent;
    private final Target target;
    private final Map<String, Target> sources;
    private final Function applyFunction;

    public Transformation(Transformer parent, Target target, Map<String, Target> sources, Function applyFunction) {
        this.parent = parent;
        this.target = target;
        this.sources = sources;
        this.applyFunction = applyFunction;
    }

    public Transformer getParent() {
        return parent;
    }

    public Target getTarget() {
        return target;
    }

    public Map<String, Target> getSources() {
        return sources;
    }

    public Node apply(Node target, Map<String, Node> sources) {
        return applyFunction.apply(target, sources);
    }

    public interface Function {
        Node apply(Node target, Map<String, Node> sources);
    }
}
