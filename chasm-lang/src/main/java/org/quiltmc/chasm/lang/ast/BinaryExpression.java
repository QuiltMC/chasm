package org.quiltmc.chasm.lang.ast;

import java.util.HashMap;
import java.util.Map;

import org.quiltmc.chasm.lang.ReductionContext;
import org.quiltmc.chasm.lang.op.Addable;
import org.quiltmc.chasm.lang.op.Equatable;
import org.quiltmc.chasm.lang.op.Multiplicable;
import org.quiltmc.chasm.lang.op.NumberLike;
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
            case MULTIPLY:
                if (left instanceof Multiplicable && ((Multiplicable) left).canMultiply(right)) {
                    result = ((Multiplicable) left).multiply(right);
                }
                break;
            case DIVIDE:
                if (left instanceof NumberLike && ((NumberLike) left).canDivide(right)) {
                    result = ((NumberLike) left).divide(right);
                }
                break;
            case MOD:
                if (left instanceof NumberLike && ((NumberLike) left).canModulo(right)) {
                    result = ((NumberLike) left).modulo(right);
                }
            case EQUAL:
                if (left instanceof NoneExpression) {
                    result = new ConstantBooleanExpression(right instanceof NoneExpression);
                } else if (right instanceof NoneExpression) {
                    result = new ConstantBooleanExpression(false);
                } else if (left instanceof Equatable && ((Equatable) left).canEquate(right)) {
                    result = ((Equatable) left).equate(right);
                }
                break;
            case NOT_EQUAL:
                if (left instanceof NoneExpression) {
                    result = new ConstantBooleanExpression(!(right instanceof NoneExpression));
                } else if (right instanceof NoneExpression) {
                    result = new ConstantBooleanExpression(true);
                } else if (left instanceof Equatable && ((Equatable) left).canEquate(right)) {
                    result = new UnaryExpression (UnaryExpression.Operation.NOT, ((Equatable) left).equate(right));
                }
                break;
            case LESS_THAN:
                if (left instanceof NumberLike && ((NumberLike) left).canCompare(right)) {
                    result = ((NumberLike) left).lessThan(right);
                }
                break;
            case LESS_THAN_EQUAL:
                if (left instanceof NumberLike && ((NumberLike) left).canCompare(right)) {
                    result = ((NumberLike) left).lessThanOrEqual(right);
                }
                break;
            case GREATER_THAN:
                if (left instanceof NumberLike && ((NumberLike) left).canCompare(right)) {
                    result = ((NumberLike) left).greaterThan(right);
                }
                break;
            case GREATER_THAN_EQUAL:
                if (left instanceof NumberLike && ((NumberLike) left).canCompare(right)) {
                    result = ((NumberLike) left).greaterThanOrEqual(right);
                }
                break;
            case AND:
                if (left instanceof NumberLike && ((NumberLike) left).canBitwiseAnd(right)) {
                    result = ((NumberLike) left).bitwiseAnd(right);
                }
                break;
            case OR:
                if (left instanceof NumberLike && ((NumberLike) left).canBitwiseOr(right)) {
                    result = ((NumberLike) left).bitwiseOr(right);
                }
                break;
            case XOR:
                if (left instanceof NumberLike && ((NumberLike) left).canBitwiseXor(right)) {
                    result = ((NumberLike) left).bitwiseXor(right);
                }
                break;
            case SHL:
                if (left instanceof NumberLike && ((NumberLike) left).canBitwiseSHL(right)) {
                    result = ((NumberLike) left).bitwiseSHL(right);
                }
                break;
            case SHR:
                if (left instanceof NumberLike && ((NumberLike) left).canBitwiseSHR(right)) {
                    result = ((NumberLike) left).bitwiseSHR(right);
                }
                break;
            case USHR:
                if (left instanceof NumberLike && ((NumberLike) left).canBitwiseUSHR(right)) {
                    result = ((NumberLike) left).bitwiseUSHR(right);
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
        MOD("%"),
        ADD("+"),
        SUBTRACT("-"),
        LESS_THAN("<"),
        LESS_THAN_EQUAL("<="),
        EQUAL("="),
        NOT_EQUAL("!="),
        GREATER_THAN_EQUAL(">="),
        GREATER_THAN(">"),
        AND("&"),
        OR("|"),
        XOR("^"),
        SHL("<<"),
        USHR(">>>"),
        SHR(">>"),
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
            final Operation operation = tokenToOperation.get(token);
            if (operation == null) {
                throw new RuntimeException("Unknown operation: " + token);
            }
            return operation;
        }

        public String getToken() {
            return token;
        }
    }
}
