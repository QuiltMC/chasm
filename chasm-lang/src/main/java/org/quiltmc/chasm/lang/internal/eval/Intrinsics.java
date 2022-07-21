package org.quiltmc.chasm.lang.internal.eval;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.quiltmc.chasm.lang.api.ast.Expression;
import org.quiltmc.chasm.lang.api.ast.ListExpression;
import org.quiltmc.chasm.lang.api.ast.LiteralExpression;
import org.quiltmc.chasm.lang.api.eval.Evaluator;
import org.quiltmc.chasm.lang.api.eval.FunctionExpression;
import org.quiltmc.chasm.lang.internal.render.RendererConfig;

public class Intrinsics {
    static final Map<String, Expression> INTRINSICS;
    public static final Scope SCOPE;

    static {
        INTRINSICS = new HashMap<>();
        INTRINSICS.put("chars", new CharsFunction());
        INTRINSICS.put("join", new JoinFunction());

        SCOPE = new Scope(null, INTRINSICS);
    }

    static class CharsFunction extends FunctionExpression {
        @Override
        public Expression apply(Evaluator evaluator, Expression expression) {
            if (!(expression instanceof LiteralExpression)
                    || !(((LiteralExpression) expression).getValue() instanceof String)) {
                throw new EvaluationException(
                        "Built-in function \"chars\" can only be applied to Strings but found " + expression);
            }

            String value = (String) ((LiteralExpression) expression).getValue();

            List<Expression> entries = new ArrayList<>();
            for (int i = 0; i < value.length(); i++) {
                entries.add(new LiteralExpression((long) value.charAt(i)));
            }

            return new ListExpression(entries);
        }

        @Override
        public void render(RendererConfig config, StringBuilder builder, int currentIndentationMultiplier) {

        }
    }

    static class JoinFunction extends FunctionExpression {
        @Override
        public Expression apply(Evaluator evaluator, Expression expression) {
            if (!(expression instanceof ListExpression)) {
                throw new EvaluationException(
                        "Built-in function \"join\" can only be applied to list of integers but found " + expression);
            }

            List<Expression> entries = ((ListExpression) expression).getEntries().stream().map(evaluator::reduce)
                    .collect(Collectors.toList());

            if (!entries.stream().allMatch(
                    e -> e instanceof LiteralExpression && ((LiteralExpression) e).getValue() instanceof Long)) {
                throw new EvaluationException(
                        "Built-in function \"join\" can only be applied to list of integers but found " + expression);
            }

            String joined = entries.stream()
                    .map(e -> Character.toString((char) ((Long) ((LiteralExpression) e).getValue()).shortValue()))
                    .collect(Collectors.joining());

            return new LiteralExpression(joined);
        }

        @Override
        public void render(RendererConfig config, StringBuilder builder, int currentIndentationMultiplier) {

        }
    }
}
