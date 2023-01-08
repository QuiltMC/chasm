package org.quiltmc.chasm.lang.api.ast;

import java.util.Map;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.chasm.lang.api.eval.Evaluator;
import org.quiltmc.chasm.lang.api.eval.Resolver;
import org.quiltmc.chasm.lang.api.eval.SourceSpan;
import org.quiltmc.chasm.lang.api.exception.EvaluationException;
import org.quiltmc.chasm.lang.internal.render.RenderUtil;
import org.quiltmc.chasm.lang.internal.render.Renderer;

public class MemberNode extends Node {
    private Node left;
    private String identifier;

    public MemberNode(Node node, String identifier) {
        this.left = node;
        this.identifier = identifier;
    }

    public Node getLeft() {
        return left;
    }

    public void setLeft(Node left) {
        this.left = left;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public void render(Renderer renderer, StringBuilder builder, int currentIndentationMultiplier) {
        left.render(renderer, builder, currentIndentationMultiplier);
        builder.append(".").append(RenderUtil.quotifyIdentifierIfNeeded(identifier, '`'));
    }

    @Override
    @ApiStatus.OverrideOnly
    public void resolve(Resolver resolver) {
        left.resolve(resolver);
    }

    @Override
    @ApiStatus.OverrideOnly
    public Node evaluate(Evaluator evaluator) {
        Node left = this.left.evaluate(evaluator);

        if (!(left instanceof MapNode)) {
            throw new EvaluationException(
                    "Member access expected a map, but got a " + left,
                    left.getMetadata().get(SourceSpan.class));
        }

        Map<String, Node> entries = ((MapNode) left).getEntries();

        if (!entries.containsKey(identifier)) {
            return new NullNode();
        }

        return entries.get(identifier).evaluate(evaluator);
    }
}
