package org.quiltmc.chasm.lang.ast;

import java.util.HashMap;
import java.util.Map;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.quiltmc.chasm.lang.Cache;
import org.quiltmc.chasm.lang.ScopeStack;
import org.quiltmc.chasm.lang.antlr.ChasmParser;
import org.quiltmc.chasm.lang.op.AddableExpression;
import org.quiltmc.chasm.lang.op.EquatableExpression;
import org.quiltmc.chasm.lang.op.Expression;
import org.quiltmc.chasm.lang.op.MultiplicableExpression;
import org.quiltmc.chasm.lang.op.NumberLike;
import org.quiltmc.chasm.lang.op.Subtractable;

public class BinaryExpression extends AbstractExpression {
    private final Expression left;
    private final Operation operation;
    private final Expression right;

    public BinaryExpression(ParseTree tree, Expression left, Operation operation, Expression right) {
        super(tree);
        this.left = left;
        this.operation = operation;
        this.right = right;
    }

    @Override
    public BinaryExpression resolve(ScopeStack scope) {
        return new BinaryExpression(getParseTree(), left.resolve(scope), operation, right.resolve(scope));
    }

    @Override
    public Expression reduce(Cache cache) {
        Expression left = cache.reduceCached(this.left);
        Expression right = cache.reduceCached(this.right);

        Expression result = null;

        switch (operation) {
            case ADD:
                if (left instanceof AddableExpression && ((AddableExpression) left).canAdd(right)) {
                    result = ((AddableExpression) left).add(getParseTree(), right);
                }
                break;
            case SUBTRACT:
                if (left instanceof Subtractable && ((Subtractable) left).canSubtract(right)) {
                    result = ((Subtractable) left).subtract(getParseTree(), right);
                }
                break;
            case MULTIPLY:
                if (left instanceof MultiplicableExpression && ((MultiplicableExpression) left).canMultiply(right)) {
                    result = ((MultiplicableExpression) left).multiply(getParseTree(), right);
                }
                break;
            case DIVIDE:
                if (left instanceof NumberLike && ((NumberLike) left).canDivide(right)) {
                    result = ((NumberLike) left).divide(getParseTree(), right);
                }
                break;
            case MOD:
                if (left instanceof NumberLike && ((NumberLike) left).canModulo(right)) {
                    result = ((NumberLike) left).modulo(getParseTree(), right);
                }
                break;
            case EQUAL:
                if (left instanceof NullExpression) {
                    result = new ConstantBooleanExpression(getParseTree(), right instanceof NullExpression);
                } else if (right instanceof NullExpression) {
                    result = new ConstantBooleanExpression(getParseTree(), false);
                } else if (left instanceof EquatableExpression && ((EquatableExpression) left).canEquate(right)) {
                    result = ((EquatableExpression) left).equate(getParseTree(), right);
                }
                break;
            case NOT_EQUAL:
                if (left instanceof NullExpression) {
                    result = new ConstantBooleanExpression(getParseTree(), !(right instanceof NullExpression));
                } else if (right instanceof NullExpression) {
                    result = new ConstantBooleanExpression(getParseTree(), true);
                } else if (left instanceof EquatableExpression && ((EquatableExpression) left).canEquate(right)) {
                    result = new ConstantBooleanExpression(getParseTree(),
                            !((EquatableExpression) left).equate(getParseTree(), right).value);
                }
                break;
            case LESS_THAN:
                if (left instanceof NumberLike && ((NumberLike) left).canCompare(right)) {
                    result = ((NumberLike) left).lessThan(getParseTree(), right);
                }
                break;
            case LESS_THAN_EQUAL:
                if (left instanceof NumberLike && ((NumberLike) left).canCompare(right)) {
                    result = ((NumberLike) left).lessThanOrEqual(getParseTree(), right);
                }
                break;
            case GREATER_THAN:
                if (left instanceof NumberLike && ((NumberLike) left).canCompare(right)) {
                    result = ((NumberLike) left).greaterThan(getParseTree(), right);
                }
                break;
            case GREATER_THAN_EQUAL:
                if (left instanceof NumberLike && ((NumberLike) left).canCompare(right)) {
                    result = ((NumberLike) left).greaterThanOrEqual(getParseTree(), right);
                }
                break;
            case BITWISE_AND:
                if (left instanceof NumberLike && ((NumberLike) left).canBitwiseAnd(right)) {
                    result = ((NumberLike) left).bitwiseAnd(getParseTree(), right);
                }
                break;
            case BITWISE_OR:
                if (left instanceof NumberLike && ((NumberLike) left).canBitwiseOr(right)) {
                    result = ((NumberLike) left).bitwiseOr(getParseTree(), right);
                }
                break;
            case BITWISE_XOR:
                if (left instanceof NumberLike && ((NumberLike) left).canBitwiseXor(right)) {
                    result = ((NumberLike) left).bitwiseXor(getParseTree(), right);
                }
                break;
            case SHIFT_LEFT:
                if (left instanceof NumberLike && ((NumberLike) left).canLeftShift(right)) {
                    result = ((NumberLike) left).leftShift(getParseTree(), right);
                }
                break;
            case SHIFT_RIGHT:
                if (left instanceof NumberLike && ((NumberLike) left).canRightShift(right)) {
                    result = ((NumberLike) left).rightShift(getParseTree(), right);
                }
                break;
            case UNSIGNED_SHIFT_RIGHT:
                if (left instanceof NumberLike && ((NumberLike) left).canUnsignedRightShift(right)) {
                    result = ((NumberLike) left).unsignedRightShift(getParseTree(), right);
                }
                break;
            default:
        }

        if (result == null) {
            throw new RuntimeException("Operation " + operation + " is not implemented for "
                    + left.getClass().getSimpleName() + " and " + right.getClass().getSimpleName());
        }

        return cache.reduceCached(result);
    }

    public enum Operation {
        MULTIPLY(ChasmParser.MULTIPLY),
        DIVIDE(ChasmParser.DIVIDE),
        MOD(ChasmParser.MODULO),
        ADD(ChasmParser.PLUS),
        SUBTRACT(ChasmParser.MINUS),
        LESS_THAN(ChasmParser.LESS_THAN),
        LESS_THAN_EQUAL(ChasmParser.LESS_THAN_EQUAL),
        EQUAL(ChasmParser.EQUAL),
        NOT_EQUAL(ChasmParser.NOT_EQUAL),
        GREATER_THAN(ChasmParser.GREATER_THAN),
        GREATER_THAN_EQUAL(ChasmParser.GREATER_THAN_EQUAL),
        BITWISE_AND(ChasmParser.BITWISE_AND),
        BITWISE_OR(ChasmParser.BITWISE_OR),
        BITWISE_XOR(ChasmParser.BITWISE_XOR),
        SHIFT_LEFT(ChasmParser.SHIFT_LEFT),
        UNSIGNED_SHIFT_RIGHT(ChasmParser.UNSIGNED_SHIFT_RIGHT),
        SHIFT_RIGHT(ChasmParser.SHIFT_RIGHT),
        ;

        private static final Map<Integer, Operation> tokenTypeToOperation = new HashMap<>();

        static {
            for (Operation op : Operation.values()) {
                tokenTypeToOperation.put(op.tokenType, op);
            }
        }

        private final int tokenType;

        Operation(int tokenType) {
            this.tokenType = tokenType;
        }

        public static Operation fromToken(Token token) {
            Operation operation = tokenTypeToOperation.get(token.getType());
            if (operation == null) {
                throw new RuntimeException("Unknown operation: " + token);
            }
            return operation;
        }

        public String toString() {
            return ChasmParser.VOCABULARY.getDisplayName(tokenType);
        }
    }
}
