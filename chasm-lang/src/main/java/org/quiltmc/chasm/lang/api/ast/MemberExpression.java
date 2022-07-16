package org.quiltmc.chasm.lang.api.ast;

import java.util.Objects;

public class MemberExpression extends Expression {
    private Expression left;
    private String identifier;

    public MemberExpression(Expression expression, String identifier) {
        this.left = expression;
        this.identifier = identifier;
    }

    public Expression getLeft() {
        return left;
    }

    public void setLeft(Expression left) {
        this.left = left;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public MemberExpression copy() {
        return new MemberExpression(left.copy(), identifier);
    }
}
