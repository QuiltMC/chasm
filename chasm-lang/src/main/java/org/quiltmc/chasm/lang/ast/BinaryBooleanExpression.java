package org.quiltmc.chasm.lang.ast;

import java.util.HashMap;
import java.util.Map;

import org.quiltmc.chasm.lang.ReductionContext;

public class BinaryBooleanExpression implements Expression {
    private final Expression left;
    private final Operation operation;
    private final Expression right;

    public BinaryBooleanExpression(Expression left, Operation operation, Expression right) {
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
        Expression right = null;

        switch (operation) {
            case AND:
                if (left instanceof ConstantBooleanExpression && ((ConstantBooleanExpression) left).value) {
                    right = this.right.reduce(context);
                    if (right instanceof ConstantBooleanExpression) {
                        return right;
                    }
                }
                break;
            case OR:
                if (left instanceof ConstantBooleanExpression) {
                    if (((ConstantBooleanExpression) left).value) {
                        return left;
                    }
                    right = this.right.reduce(context);
                    if (right instanceof ConstantBooleanExpression) {
                        return right;
                    }
                }
                break;
            default:
        }

        if (right == null) {
            throw new RuntimeException("Operation " + operation.getToken() + " is not implemented for "
                    + left.getClass().getSimpleName() + " as left hand side");
        } else {
            throw new RuntimeException("Operation " + operation.getToken() + " is not implemented for "
                    + left.getClass().getSimpleName() + " and " + right.getClass().getSimpleName());
        }
    }

    @Override
    public BinaryBooleanExpression copy() {
        return new BinaryBooleanExpression(left.copy(), operation, right.copy());
    }

    public enum Operation {
        AND("&&"),
        OR("||"),
        ;

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
