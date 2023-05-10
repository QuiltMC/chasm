package org.quiltmc.chasm.lang.api.ast;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.chasm.lang.api.eval.Evaluator;
import org.quiltmc.chasm.lang.api.eval.Resolver;
import org.quiltmc.chasm.lang.api.eval.SourceSpan;
import org.quiltmc.chasm.lang.api.exception.EvaluationException;
import org.quiltmc.chasm.lang.internal.render.RenderUtil;
import org.quiltmc.chasm.lang.internal.render.Renderer;

/**
 * A reference expression, which loads the value of a symbol ("variable"), e.g. a lambda parameter or a key from an
 * outer map.
 */
public class ReferenceNode extends Node {
    private String identifier;
    private boolean global;

    /**
     * Creates a reference expression.
     *
     * @see Ast#ref(String)
     * @see Ast#globalRef(String)
     */
    public ReferenceNode(String identifier, boolean global) {
        this.identifier = identifier;
        this.global = global;
    }

    /**
     * Gets the reference name.
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Sets the reference name.
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * Gets whether the reference is global.
     *
     * <p>If a reference is global, it is resolved from the outermost scope to the innermost; if a reference is not
     * global, it is resolved from the innermost scope to the outermost.
     */
    public boolean isGlobal() {
        return global;
    }

    /**
     * Sets whether the reference is global. See {@linkplain #isGlobal()} for details.
     */
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
        Node resolved = evaluator.resolveReference(this);

        if (resolved == null) {
            throw new EvaluationException(
                    "Failed to resolve reference: " + this,
                    this.getMetadata().get(SourceSpan.class)
            );
        }

        return resolved.evaluate(evaluator);
    }

    @Override
    public void render(Renderer renderer, StringBuilder builder, int currentIndentationMultiplier) {
        if (global) {
            builder.append("$");
        }

        builder.append(RenderUtil.quotifyIdentifierIfNeeded(identifier, '`'));
    }

    @Override
    public String toString() {
        return "Ref<" + identifier + ">";
    }
}
