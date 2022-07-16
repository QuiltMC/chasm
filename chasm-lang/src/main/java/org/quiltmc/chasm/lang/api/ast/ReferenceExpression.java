package org.quiltmc.chasm.lang.api.ast;

import java.util.Objects;

public class ReferenceExpression extends Expression {
    private String identifier;
    private boolean global;

    public ReferenceExpression(String identifier, boolean global) {
        this.identifier = identifier;
        this.global = global;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public boolean isGlobal() {
        return global;
    }

    public void setGlobal(boolean global) {
        this.global = global;
    }

    @Override
    public Expression copy() {
        return new ReferenceExpression(identifier, global);
    }
}
