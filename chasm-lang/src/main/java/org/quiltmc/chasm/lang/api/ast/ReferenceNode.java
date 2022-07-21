package org.quiltmc.chasm.lang.api.ast;

import org.quiltmc.chasm.lang.internal.render.Renderer;
import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.chasm.lang.api.eval.Evaluator;
import org.quiltmc.chasm.lang.api.eval.Resolver;

public class ReferenceNode extends Node {
    private String identifier;
    private boolean global;

    public ReferenceNode(String identifier, boolean global) {
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
    @ApiStatus.OverrideOnly
    public void resolve(Resolver resolver) {
        resolver.resolveReference(this);
    }

    @Override
    @ApiStatus.OverrideOnly
    public Node evaluate(Evaluator evaluator) {
        return evaluator.resolveReference(this).evaluate(evaluator);
    }

    @Override
    public void render(Renderer renderer, StringBuilder builder, int currentIndentationMultiplier) {
        if (global) {
            builder.append("$");
        }

        builder.append(identifier);
    }
}
