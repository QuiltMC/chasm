package org.quiltmc.chasm.lang.internal.eval;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.quiltmc.chasm.lang.api.ast.BinaryExpression;
import org.quiltmc.chasm.lang.api.ast.CallExpression;
import org.quiltmc.chasm.lang.api.ast.Expression;
import org.quiltmc.chasm.lang.api.ast.IndexExpression;
import org.quiltmc.chasm.lang.api.ast.LambdaExpression;
import org.quiltmc.chasm.lang.api.ast.ListExpression;
import org.quiltmc.chasm.lang.api.ast.LiteralExpression;
import org.quiltmc.chasm.lang.api.ast.MapExpression;
import org.quiltmc.chasm.lang.api.ast.MemberExpression;
import org.quiltmc.chasm.lang.api.ast.ReferenceExpression;
import org.quiltmc.chasm.lang.api.ast.TernaryExpression;
import org.quiltmc.chasm.lang.api.ast.UnaryExpression;
import org.quiltmc.chasm.lang.api.eval.Evaluator;
import org.quiltmc.chasm.lang.api.eval.FunctionExpression;

public class EvaluatorImpl implements Evaluator {
    private final Scope rootScope;
    private final Map<ReferenceExpression, Expression> resolvedReferences = new HashMap<>();
    private final ArrayDeque<CallStackEntry> callStack = new ArrayDeque<>();

    static class CallStackEntry {
        private final LambdaExpression lambdaExpression;
        private final Expression arg;

        CallStackEntry(LambdaExpression lambdaExpression, Expression arg) {
            this.lambdaExpression = lambdaExpression;
            this.arg = arg;
        }
    }

    static class FunctionArgReference extends Expression {
        private final LambdaExpression lambdaExpression;

        FunctionArgReference(LambdaExpression lambdaExpression) {
            this.lambdaExpression = lambdaExpression;
        }

        @Override
        public Expression copy() {
            return new FunctionArgReference(lambdaExpression);
        }
    }

    public EvaluatorImpl(Map<String, Expression> globals) {
        rootScope = new Scope(Intrinsics.SCOPE, globals);
    }

    public Expression evaluate(Expression expression) {
        resolve(rootScope, expression);
        return reduce(expression);
    }

    private void resolve(Scope scope, Expression expression) {
        if (expression instanceof BinaryExpression) {
            BinaryExpression binaryExpression = (BinaryExpression) expression;
            resolve(scope, binaryExpression.getLeft());
            resolve(scope, binaryExpression.getRight());
        } else if (expression instanceof CallExpression) {
            CallExpression callExpression = (CallExpression) expression;
            resolve(scope, callExpression.getFunction());
            resolve(scope, callExpression.getArg());
        } else if (expression instanceof IndexExpression) {
            IndexExpression indexExpression = (IndexExpression) expression;
            resolve(scope, indexExpression.getLeft());
            resolve(scope, indexExpression.getIndex());
        } else if (expression instanceof LambdaExpression) {
            LambdaExpression lambdaExpression = (LambdaExpression) expression;
            Scope innerScope = new Scope(
                    scope,
                    Collections.singletonMap(
                            lambdaExpression.getIdentifier(),
                            new FunctionArgReference(lambdaExpression))
            );
            resolve(innerScope, lambdaExpression.getInner());
        } else if (expression instanceof ListExpression) {
            ListExpression listExpression = (ListExpression) expression;
            listExpression.getEntries().forEach(e -> resolve(scope, e));
        } else if (expression instanceof LiteralExpression) {
            // Leaf node
        } else if (expression instanceof MapExpression) {
            MapExpression mapExpression = (MapExpression) expression;
            Scope innerScope = new Scope(scope, mapExpression.getEntries());
            mapExpression.getEntries().forEach((i, e) -> resolve(innerScope, e));
        } else if (expression instanceof MemberExpression) {
            MemberExpression memberExpression = (MemberExpression) expression;
            resolve(scope, memberExpression.getLeft());
        } else if (expression instanceof ReferenceExpression) {
            ReferenceExpression referenceExpression = (ReferenceExpression) expression;
            Expression resolved = scope.resolve(referenceExpression.getIdentifier(), referenceExpression.isGlobal());
            resolvedReferences.putIfAbsent(referenceExpression, resolved);
        } else if (expression instanceof TernaryExpression) {
            TernaryExpression ternaryExpression = (TernaryExpression) expression;
            resolve(scope, ternaryExpression.getCondition());
            resolve(scope, ternaryExpression.getTrue());
            resolve(scope, ternaryExpression.getFalse());
        } else if (expression instanceof UnaryExpression) {
            UnaryExpression unaryExpression = (UnaryExpression) expression;
            resolve(scope, unaryExpression.getInner());
        } else {
            throw new EvaluationException("Unexpected expression type: " + expression);
        }
    }

    public Expression reduce(Expression expression) {
        Expression reduced;

        if (expression instanceof LiteralExpression) {
            // Literals are irreducible
            reduced = expression;
        } else if (expression instanceof LambdaExpression) {
            // Lambdas are irreducible
            reduced = expression;
        } else if (expression instanceof ListExpression) {
            // Reduce Lists recursively
            reduced = reduceList((ListExpression) expression);
        } else if (expression instanceof MapExpression) {
            // Reduce maps recursively
            reduced = reduceMap((MapExpression) expression);
        } else if (expression instanceof FunctionExpression) {
            // functions are irreducible
            reduced = expression;
        } else if (expression instanceof ReferenceExpression) {
            // Resolve references
            reduced = reduceReference((ReferenceExpression) expression);
        } else if (expression instanceof CallExpression) {
            // Reduce function calls
            reduced = reduceCall((CallExpression) expression);
        } else if (expression instanceof IndexExpression) {
            // Reduce index expressions
            reduced = reduceIndex((IndexExpression) expression);
        } else if (expression instanceof MemberExpression) {
            // Reduce member expressions
            reduced = reduceMember((MemberExpression) expression);
        } else if (expression instanceof UnaryExpression) {
            // Reduce unary expressions
            reduced = reduceUnary((UnaryExpression) expression);
        } else if (expression instanceof BinaryExpression) {
            // Reduce binary expressions
            reduced = reduceBinary((BinaryExpression) expression);
        } else if (expression instanceof TernaryExpression) {
            // Reduce ternary expressions
            reduced = reduceTernary((TernaryExpression) expression);
        } else {
            throw new EvaluationException("Unexpected expression type: " + expression);
        }

        return reduced;
    }

    private Expression reduceReference(ReferenceExpression referenceExpression) {
        Expression resolved = resolvedReferences.get(referenceExpression);

        if (resolved instanceof FunctionArgReference) {
            LambdaExpression function = ((FunctionArgReference) resolved).lambdaExpression;
            resolved = null;
            for (CallStackEntry entry : callStack) {
                if (entry.lambdaExpression == function) {
                    resolved = entry.arg;
                    break;
                }
            }
        }

        if (resolved == null) {
            throw new EvaluationException("Unresolved reference " + referenceExpression.getIdentifier());
        }

        return resolved;
    }

    private Expression reduceCall(CallExpression callExpression) {
        Expression leftExpression = reduce(callExpression.getFunction());
        Expression argExpression = reduce(callExpression.getArg());

        if (leftExpression instanceof LambdaExpression) {
            LambdaExpression lambdaExpression = (LambdaExpression) leftExpression;
            Expression body = lambdaExpression.getInner();
            callStack.push(new CallStackEntry(lambdaExpression, argExpression));
            Expression result = reduce(body);
            callStack.pop();
            return result;
        } else if (leftExpression instanceof FunctionExpression) {
            return reduce(((FunctionExpression) leftExpression).apply(this, argExpression));
        } else {
            throw new EvaluationException("Can only call functions, but found " + leftExpression);
        }
    }

    private Expression reduceList(ListExpression listExpression) {
        List<Expression> entries = listExpression.getEntries();
        List<Expression> newEntries = new ArrayList<>();

        for (Expression entry : entries) {
            newEntries.add(reduce(entry));
        }

        return new ListExpression(newEntries);
    }

    private Expression reduceMap(MapExpression mapExpression) {
        Map<String, Expression> entries = mapExpression.getEntries();
        Map<String, Expression> newEntries = new LinkedHashMap<>();

        for (Map.Entry<String, Expression> entry : entries.entrySet()) {
            newEntries.put(entry.getKey(), reduce(entry.getValue()));
        }

        return new MapExpression(newEntries);
    }

    private Expression reduceIndex(IndexExpression indexExpression) {
        Expression leftExpression = reduce(indexExpression.getLeft());
        Expression rightExpression = reduce(indexExpression.getIndex());

        // Index list
        if (leftExpression instanceof ListExpression && rightExpression instanceof LiteralExpression
                && ((LiteralExpression) rightExpression).getValue() instanceof Long) {
            long index = (Long) ((LiteralExpression) rightExpression).getValue();
            List<Expression> entries = ((ListExpression) leftExpression).getEntries();

            if (index < 0 || index >= entries.size()) {
                return new LiteralExpression(null);
            }

            return reduce(entries.get((int) index));
        }

        // Filter list
        if (leftExpression instanceof ListExpression && rightExpression instanceof LambdaExpression) {
            LambdaExpression lambdaExpression = (LambdaExpression) rightExpression;
            List<Expression> entries = ((ListExpression) leftExpression).getEntries();
            List<Expression> newEntries = new ArrayList<>();

            for (Expression entry : entries) {
                CallExpression callExpression = new CallExpression(lambdaExpression, entry);
                Expression reduced = reduce(callExpression);
                if (!(reduced instanceof LiteralExpression)
                        || !(((LiteralExpression) reduced).getValue() instanceof Boolean)) {
                    throw new EvaluationException("Filter function must return a boolean but found " + reduced);
                }

                boolean result = (boolean) ((LiteralExpression) reduced).getValue();
                if (result) {
                    newEntries.add(entry);
                }
            }

            return reduce(new ListExpression(newEntries));
        }

        // Index map
        if (leftExpression instanceof MapExpression && rightExpression instanceof LiteralExpression
                && ((LiteralExpression) rightExpression).getValue() instanceof String) {
            String key = (String) ((LiteralExpression) rightExpression).getValue();
            Map<String, Expression> entries = ((MapExpression) leftExpression).getEntries();

            if (!entries.containsKey(key)) {
                return new LiteralExpression(null);
            }

            return reduce(entries.get(key));
        }

        throw new EvaluationException("Can't index " + leftExpression + " with " + rightExpression);
    }

    private Expression reduceMember(MemberExpression memberExpression) {
        Expression leftExpression = reduce(memberExpression.getLeft());
        String key = memberExpression.getIdentifier();

        if (!(leftExpression instanceof MapExpression)) {
            throw new EvaluationException("Member access expected a map, but got a " + leftExpression);
        }

        Map<String, Expression> entries = ((MapExpression) leftExpression).getEntries();

        if (!entries.containsKey(key)) {
            throw new EvaluationException("Map doesn't contain member " + key);
        }

        return reduce(entries.get(key));
    }

    private Expression reduceUnary(UnaryExpression unaryExpression) {
        Expression inner = reduce(unaryExpression.getInner());

        if (!(inner instanceof LiteralExpression)) {
            throw new EvaluationException("Unary expression can only be applied to values but found " + inner);
        }

        Object value = ((LiteralExpression) inner).getValue();

        switch (unaryExpression.getOperator()) {
            case PLUS: {
                if (value instanceof Long || value instanceof Double) {
                    return inner;
                }
                throw new EvaluationException(
                        "Unary plus operator can only be applied to integers and floats but found " + value.getClass()
                );
            }
            case MINUS: {
                if (value instanceof Long) {
                    return new LiteralExpression(-(Long) value);
                }
                if (value instanceof Double) {
                    return new LiteralExpression(-(Double) value);
                }
                throw new EvaluationException(
                        "Unary minus operator can only be applied to integers and floats but found " + value.getClass()
                );
            }
            case NOT: {
                if (value instanceof Boolean) {
                    return new LiteralExpression(!(Boolean) value);
                }
                throw new EvaluationException(
                        "Unary not operator can only be applied to booleans but found " + value.getClass()
                );
            }
            case INVERT: {
                if (value instanceof Long) {
                    return new LiteralExpression(~(Long) value);
                }
                throw new EvaluationException(
                        "Unary invert operator can only be applied to integers but found " + value.getClass()
                );
            }
            default: {
                throw new EvaluationException(
                        "Unknown unary operator " + unaryExpression.getOperator()
                );
            }
        }
    }

    private Expression reduceBinary(BinaryExpression binaryExpression) {
        BinaryExpression.Operator operator = binaryExpression.getOperator();

        Expression leftExpression = reduce(binaryExpression.getLeft());
        Object leftValue = null;

        if (leftExpression instanceof LiteralExpression) {
            leftValue = ((LiteralExpression) leftExpression).getValue();
        }

        // Short-circuiting operators
        if (operator == BinaryExpression.Operator.BOOLEAN_AND || operator == BinaryExpression.Operator.BOOLEAN_OR) {
            if (!(leftValue instanceof Boolean)) {
                throw new EvaluationException("The left side of boolean operator " + operator
                        + " must be a boolean but found " + leftExpression);
            }

            if (operator == BinaryExpression.Operator.BOOLEAN_OR && (Boolean) leftValue) {
                return leftExpression;
            }

            if (operator == BinaryExpression.Operator.BOOLEAN_AND && !(Boolean) leftValue) {
                return leftExpression;
            }

            return reduce(binaryExpression.getRight());
        }

        Expression rightExpression = reduce(binaryExpression.getRight());
        Object rightValue = null;

        if (rightExpression instanceof LiteralExpression) {
            rightValue = ((LiteralExpression) rightExpression).getValue();
        }

        if (operator == BinaryExpression.Operator.PLUS) {
            if (leftExpression instanceof ListExpression && rightExpression instanceof ListExpression) {
                List<Expression> leftEntries = ((ListExpression) leftExpression).getEntries();
                List<Expression> rightEntries = ((ListExpression) rightExpression).getEntries();

                ArrayList<Expression> newEntries = new ArrayList<>(leftEntries);
                newEntries.addAll(rightEntries);

                return new ListExpression(newEntries);
            }

            if (leftValue instanceof Long && rightValue instanceof Long) {
                return new LiteralExpression((Long) leftValue + (Long) rightValue);
            }

            if (leftValue instanceof Double && rightValue instanceof Double) {
                return new LiteralExpression((Double) leftValue + (Double) rightValue);
            }

            if (leftValue instanceof Long && rightValue instanceof Double) {
                return new LiteralExpression((Long) leftValue + (Double) rightValue);
            }

            if (leftValue instanceof Double && rightValue instanceof Long) {
                return new LiteralExpression((Double) leftValue + (Long) rightValue);
            }

            if (leftValue instanceof String || rightValue instanceof String) {
                return new LiteralExpression(String.valueOf(leftValue) + rightValue);
            }
        }

        if (operator == BinaryExpression.Operator.MINUS) {
            if (leftValue instanceof Long && rightValue instanceof Long) {
                return new LiteralExpression((Long) leftValue - (Long) rightValue);
            }

            if (leftValue instanceof Double && rightValue instanceof Double) {
                return new LiteralExpression((Double) leftValue - (Double) rightValue);
            }

            if (leftValue instanceof Long && rightValue instanceof Double) {
                return new LiteralExpression((Long) leftValue - (Double) rightValue);
            }

            if (leftValue instanceof Double && rightValue instanceof Long) {
                return new LiteralExpression((Double) leftValue - (Long) rightValue);
            }
        }

        if (operator == BinaryExpression.Operator.MULTIPLY) {
            if (leftValue instanceof Long && rightValue instanceof Long) {
                return new LiteralExpression((Long) leftValue * (Long) rightValue);
            }

            if (leftValue instanceof Double && rightValue instanceof Double) {
                return new LiteralExpression((Double) leftValue * (Double) rightValue);
            }

            if (leftValue instanceof Long && rightValue instanceof Double) {
                return new LiteralExpression((Long) leftValue * (Double) rightValue);
            }

            if (leftValue instanceof Double && rightValue instanceof Long) {
                return new LiteralExpression((Double) leftValue * (Long) rightValue);
            }
        }

        if (operator == BinaryExpression.Operator.DIVIDE) {
            if (leftValue instanceof Long && rightValue instanceof Long) {
                return new LiteralExpression((Long) leftValue / (Long) rightValue);
            }

            if (leftValue instanceof Double && rightValue instanceof Double) {
                return new LiteralExpression((Double) leftValue / (Double) rightValue);
            }

            if (leftValue instanceof Long && rightValue instanceof Double) {
                return new LiteralExpression((Long) leftValue / (Double) rightValue);
            }

            if (leftValue instanceof Double && rightValue instanceof Long) {
                return new LiteralExpression((Double) leftValue / (Long) rightValue);
            }
        }

        if (operator == BinaryExpression.Operator.MODULO) {
            if (leftValue instanceof Long && rightValue instanceof Long) {
                return new LiteralExpression((Long) leftValue % (Long) rightValue);
            }

            if (leftValue instanceof Double && rightValue instanceof Double) {
                return new LiteralExpression((Double) leftValue % (Double) rightValue);
            }

            if (leftValue instanceof Long && rightValue instanceof Double) {
                return new LiteralExpression((Long) leftValue % (Double) rightValue);
            }

            if (leftValue instanceof Double && rightValue instanceof Long) {
                return new LiteralExpression((Double) leftValue % (Long) rightValue);
            }
        }

        if (operator == BinaryExpression.Operator.SHIFT_LEFT) {
            if (leftValue instanceof Long && rightValue instanceof Long) {
                return new LiteralExpression((Long) leftValue << (Long) rightValue);
            }
        }

        if (operator == BinaryExpression.Operator.SHIFT_RIGHT) {
            if (leftValue instanceof Long && rightValue instanceof Long) {
                return new LiteralExpression((Long) leftValue >> (Long) rightValue);
            }
        }

        if (operator == BinaryExpression.Operator.SHIFT_RIGHT_UNSIGNED) {
            if (leftValue instanceof Long && rightValue instanceof Long) {
                return new LiteralExpression((Long) leftValue >>> (Long) rightValue);
            }
        }

        if (operator == BinaryExpression.Operator.LESS_THAN) {
            if (leftValue instanceof Long && rightValue instanceof Long) {
                return new LiteralExpression((Long) leftValue < (Long) rightValue);
            }

            if (leftValue instanceof Double && rightValue instanceof Double) {
                return new LiteralExpression((Double) leftValue < (Double) rightValue);
            }

            if (leftValue instanceof Long && rightValue instanceof Double) {
                return new LiteralExpression((Long) leftValue < (Double) rightValue);
            }

            if (leftValue instanceof Double && rightValue instanceof Long) {
                return new LiteralExpression((Double) leftValue < (Long) rightValue);
            }
        }

        if (operator == BinaryExpression.Operator.LESS_THAN_OR_EQUAL) {
            if (leftValue instanceof Long && rightValue instanceof Long) {
                return new LiteralExpression((Long) leftValue <= (Long) rightValue);
            }

            if (leftValue instanceof Double && rightValue instanceof Double) {
                return new LiteralExpression((Double) leftValue <= (Double) rightValue);
            }

            if (leftValue instanceof Long && rightValue instanceof Double) {
                return new LiteralExpression((Long) leftValue <= (Double) rightValue);
            }

            if (leftValue instanceof Double && rightValue instanceof Long) {
                return new LiteralExpression((Double) leftValue <= (Long) rightValue);
            }
        }

        if (operator == BinaryExpression.Operator.GREATER_THAN) {
            if (leftValue instanceof Long && rightValue instanceof Long) {
                return new LiteralExpression((Long) leftValue > (Long) rightValue);
            }

            if (leftValue instanceof Double && rightValue instanceof Double) {
                return new LiteralExpression((Double) leftValue > (Double) rightValue);
            }

            if (leftValue instanceof Long && rightValue instanceof Double) {
                return new LiteralExpression((Long) leftValue > (Double) rightValue);
            }

            if (leftValue instanceof Double && rightValue instanceof Long) {
                return new LiteralExpression((Double) leftValue > (Long) rightValue);
            }
        }

        if (operator == BinaryExpression.Operator.GREATER_THAN_OR_EQUAL) {
            if (leftValue instanceof Long && rightValue instanceof Long) {
                return new LiteralExpression((Long) leftValue >= (Long) rightValue);
            }

            if (leftValue instanceof Double && rightValue instanceof Double) {
                return new LiteralExpression((Double) leftValue >= (Double) rightValue);
            }

            if (leftValue instanceof Long && rightValue instanceof Double) {
                return new LiteralExpression((Long) leftValue >= (Double) rightValue);
            }

            if (leftValue instanceof Double && rightValue instanceof Long) {
                return new LiteralExpression((Double) leftValue >= (Long) rightValue);
            }
        }

        if (operator == BinaryExpression.Operator.EQUAL) {
            if (leftExpression instanceof LiteralExpression && rightExpression instanceof LiteralExpression) {
                return new LiteralExpression(Objects.equals(leftValue, rightValue));
            }
        }

        if (operator == BinaryExpression.Operator.NOT_EQUAL) {
            return new LiteralExpression(!Objects.equals(leftExpression, rightExpression));
        }

        if (operator == BinaryExpression.Operator.BITWISE_AND) {
            if (leftValue instanceof Long && rightValue instanceof Long) {
                return new LiteralExpression((Long) leftValue & (Long) rightValue);
            }
        }

        if (operator == BinaryExpression.Operator.BITWISE_XOR) {
            if (leftValue instanceof Long && rightValue instanceof Long) {
                return new LiteralExpression((Long) leftValue ^ (Long) rightValue);
            }
        }

        if (operator == BinaryExpression.Operator.BITWISE_OR) {
            if (leftValue instanceof Long && rightValue instanceof Long) {
                return new LiteralExpression((Long) leftValue | (Long) rightValue);
            }
        }

        throw new EvaluationException(
                "Can't apply operator " + operator + " to " + leftExpression + " and " + rightExpression
        );
    }

    private Expression reduceTernary(TernaryExpression ternaryExpression) {
        Expression conditionExp = reduce(ternaryExpression.getCondition());

        if (!(conditionExp instanceof LiteralExpression)
                || !(((LiteralExpression) conditionExp).getValue() instanceof Boolean)) {
            throw new EvaluationException("Condition in ternary must evaluate to a boolean but found " + conditionExp);
        }

        if ((Boolean) ((LiteralExpression) conditionExp).getValue()) {
            return reduce(ternaryExpression.getTrue());
        } else {
            return reduce(ternaryExpression.getFalse());
        }
    }
}
