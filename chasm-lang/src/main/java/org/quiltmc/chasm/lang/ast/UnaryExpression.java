package org.quiltmc.chasm.lang.ast;

import java.util.HashMap;
import java.util.Map;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.quiltmc.chasm.lang.Cache;
import org.quiltmc.chasm.lang.ScopeStack;
import org.quiltmc.chasm.lang.antlr.ChasmParser;
import org.quiltmc.chasm.lang.op.Expression;
import org.quiltmc.chasm.lang.op.NumberLike;

public class UnaryExpression extends AbstractExpression {
    private final Operation operation;
    private final Expression inner;

    public UnaryExpression(ParseTree tree, Operation operation, Expression inner) {
        super(tree);
        this.operation = operation;
        this.inner = inner;
    }

    @Override
    public Expression resolve(ScopeStack scope) {
        return new UnaryExpression(getParseTree(), operation, inner.resolve(scope));
    }

    @Override
    public Expression reduce(Cache cache) {
        Expression inner = cache.reduceCached(this.inner);

        Expression result = null;

        switch (operation) {
            case PLUS:
                result = inner;
                break;
            case MINUS: {
                if (inner instanceof NumberLike) {
                    result = ((NumberLike) inner).negate(getParseTree());
                }
                break;
            }
            case INVERT: {
                if (inner instanceof NumberLike) {
                    result = ((NumberLike) inner).invert(getParseTree());
                }
                break;
            }
            case NOT: {
                if (inner instanceof ConstantBooleanExpression) {
                    result = new ConstantBooleanExpression(getParseTree(), !((ConstantBooleanExpression) inner).value);
                }
                break;
            }
            default:
        }

        if (result == null) {
            throw new RuntimeException("Operation " + operation + " is not implemented for "
                    + inner.getClass().getSimpleName());
        }

        return cache.reduceCached(result);
    }

    public enum Operation {
        PLUS(ChasmParser.PLUS),
        MINUS(ChasmParser.MINUS),
        NOT(ChasmParser.NOT),
        INVERT(ChasmParser.INVERT),
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
