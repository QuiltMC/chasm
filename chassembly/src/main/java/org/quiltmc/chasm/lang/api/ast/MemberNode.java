package org.quiltmc.chasm.lang.api.ast;

import java.util.Map;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.chasm.lang.api.eval.Evaluator;
import org.quiltmc.chasm.lang.api.eval.Resolver;
import org.quiltmc.chasm.lang.api.exception.EvaluationException;
import org.quiltmc.chasm.lang.internal.render.OperatorPriority;
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
    public void render(Renderer renderer, StringBuilder builder, int indentation, OperatorPriority minPriority) {
        boolean needsBrackets = !OperatorPriority.ARGUMENT_PRIMARY.allowedFor(minPriority);
        if (needsBrackets) {
            builder.append('(');
        }
        left.render(renderer, builder, indentation, OperatorPriority.ARGUMENT_PRIMARY);
        builder.append(".").append(identifier);
        if (needsBrackets) {
            builder.append(')');
        }
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
            throw new EvaluationException("Member access expected a map, but got a " + left);
        }

        Map<String, Node> entries = ((MapNode) left).getEntries();

        if (!entries.containsKey(identifier)) {
            return NullNode.INSTANCE;
        }

        return entries.get(identifier).evaluate(evaluator);
    }
}
