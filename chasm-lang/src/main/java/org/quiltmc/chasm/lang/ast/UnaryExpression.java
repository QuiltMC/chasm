package org.quiltmc.chasm.lang.ast;

import org.quiltmc.chasm.lang.ReductionContext;
import org.quiltmc.chasm.lang.op.NumberLike;

import java.util.HashMap;
import java.util.Map;

public class UnaryExpression implements Expression {
    private final Operation operation;
    private final Expression inner;

    public UnaryExpression(Operation operation, Expression inner) {
        this.operation = operation;
        this.inner = inner;
    }

    @Override
    public void resolve(String identifier, Expression value) {
        this.inner.resolve(identifier, value);
    }

    @Override
    public Expression reduce(ReductionContext context) {
        Expression inner = context.reduce(this.inner);

        Expression result = null;

        switch (operation) {
            case PLUS: result = inner; break;
            case MIN: {
                if (inner instanceof NumberLike) {
                    result = ((NumberLike) inner).negate();
                    break;
                }
            }
            case INV: {
                if (inner instanceof NumberLike) {
                    result = ((NumberLike) inner).invert();
                    break;
                }
            }
            case NOT: {
                if (inner instanceof ConstantBooleanExpression) {
                    result = new ConstantBooleanExpression(!((ConstantBooleanExpression) inner).value);
                }
            }
        }

        if (result == null) {
            throw new RuntimeException("Operation " + operation.getToken() + " is not implemented for "
                    + inner.getClass().getSimpleName());
        }

        return context.reduce(result);
    }

    @Override
    public UnaryExpression copy() {
        return new UnaryExpression(operation, inner.copy());
    }

    public enum Operation {
        PLUS("+"),
        MIN("-"),
        NOT("!"),
        INV("~"),
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
