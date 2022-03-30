package org.quiltmc.chasm.lang.interop;

import java.util.Collection;

import org.antlr.v4.runtime.tree.ParseTree;
import org.quiltmc.chasm.api.tree.MapNode;
import org.quiltmc.chasm.api.tree.Node;
import org.quiltmc.chasm.lang.ast.AbstractMapExpression;
import org.quiltmc.chasm.lang.ast.NullExpression;
import org.quiltmc.chasm.lang.op.Expression;

public class MapNodeExpression extends AbstractMapExpression implements NodeExpression {
    private final MapNode mapNode;

    public MapNodeExpression(ParseTree tree, MapNode mapNode) {
        super(tree);
        this.mapNode = mapNode;
    }

    @Override
    public Node getNode() {
        return mapNode;
    }

    @Override
    public Expression get(String key) {
        Node result = mapNode.get(key);
        if (result == null) {
            return new NullExpression(getParseTree());
        } else {
            return NodeExpression.from(getParseTree(), result);
        }
    }

    @Override
    public Collection<String> getKeys() {
        return mapNode.keySet();
    }
}
