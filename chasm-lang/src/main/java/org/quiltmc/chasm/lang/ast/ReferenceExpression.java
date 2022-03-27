package org.quiltmc.chasm.lang.ast;

import org.antlr.v4.runtime.tree.ParseTree;
import org.quiltmc.chasm.lang.Cache;
import org.quiltmc.chasm.lang.ScopeStack;
import org.quiltmc.chasm.lang.op.Expression;

public class ReferenceExpression extends AbstractExpression {
    private final String identifier;
    private final ScopeStack scope;

    public ReferenceExpression(ParseTree tree, String identifier, ScopeStack scope) {
        super(tree);
        this.identifier = identifier;
        this.scope = scope;
    }

    @Override
    public String toString() {
        return '@' + identifier;
    }

    @Override
    public Expression resolve(ScopeStack scope) {
        return new ReferenceExpression(getParseTree(), identifier, scope.copy());
    }

    @Override
    public Expression reduce(Cache cache) {
        if (scope.contains(identifier)) {
            return cache.reduceCached(scope.get(identifier));
        } else {
            // TODO: Proper Error
            throw new RuntimeException("Unresolved reference '" + identifier + "'");
        }
    }
}
