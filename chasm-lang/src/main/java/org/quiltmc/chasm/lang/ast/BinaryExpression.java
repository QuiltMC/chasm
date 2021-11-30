package org.quiltmc.chasm.lang.ast;

import java.util.HashMap;
import java.util.Map;

import org.quiltmc.chasm.lang.ReductionContext;
import org.quiltmc.chasm.lang.op.Addable;
import org.quiltmc.chasm.lang.op.Equatable;
import org.quiltmc.chasm.lang.op.Subtractable;

public class BinaryExpression implements Expression {
    private final Expression left;
    private final Operation operation;
    private final Expression right;

    public BinaryExpression(Expression left, Operation operation, Expression right) {
        this.left = left;
        this.operation = operation;
        this.right = right;
    }

    @Override
    public void resolve(String identifier, Expression value) {
        left.resolve(identifier, value);
        right.resolve(identifier, value);
    }

    @Override
    public Expression reduce(ReductionContext context) {
        Expression left = context.reduce(this.left);
        Expression right = context.reduce(this.right);

        Expression result = null;

        switch (operation) {
            case ADD:
                if (left instanceof Addable && ((Addable) left).canAdd(right)) {
                    result = ((Addable) left).add(right);
                }
                break;
            case SUBTRACT:
                if (left instanceof Subtractable && ((Subtractable) left).canSubtract(right)) {
                    result = ((Subtractable) left).subtract(right);
                }
                break;
            case EQUAL:
                if (left instanceof Equatable && ((Equatable) left).canEquate(right)) {
                    result = ((Equatable) left).equate(right);
                }
                break;
            default:
        }

        if (result == null) {
            throw new RuntimeException("Operation " + operation.getToken() + " is not implemented for "
                    + left.getClass().getSimpleName() + " and " + right.getClass().getSimpleName());
        }

        return context.reduce(result);
    }

    @Override
    public BinaryExpression copy() {
        return new BinaryExpression(left.copy(), operation, right.copy());
    }

    public enum Operation {
        MULTIPLY("*"),
        DIVIDE("/"),
        ADD("+"),
        SUBTRACT("-"),
        LESS_THAN("<"),
        LESS_THAN_EQUAL("<="),
        EQUAL("="),
        GREATER_THAN_EQUAL(">="),
        GREATER_THAN(">");

        private static final Map<String, Operation> tokenToOperation = new HashMap<>();

        static {
            for (Operation op : Operation.values()) {
                tokenToOperation.put(op.token, op);
            }
        }

        private final String token;

        Operation(String token) {
            this.token = token;
        }

        public static Operation of(String token) {
            return tokenToOperation.get(token);
        }

        public String getToken() {
            return token;
        }
    }
}
