package org.quiltmc.chasm.lang.ast;

import java.util.HashMap;
import java.util.Map;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.quiltmc.chasm.lang.Cache;
import org.quiltmc.chasm.lang.ScopeStack;
import org.quiltmc.chasm.lang.antlr.ChasmParser;
import org.quiltmc.chasm.lang.op.Expression;

public class BinaryBooleanExpression extends AbstractExpression {
    private final Expression left;
    private final Operation operation;
    private final Expression right;

    public BinaryBooleanExpression(ParseTree tree, Expression left, Operation operation, Expression right) {
        super(tree);
        this.left = left;
        this.operation = operation;
        this.right = right;
    }

    @Override
    public BinaryBooleanExpression resolve(ScopeStack scope) {
        return new BinaryBooleanExpression(getParseTree(), left.resolve(scope), operation, right.resolve(scope));
    }

    @Override
    public ConstantBooleanExpression reduce(Cache cache) {
        Expression left = cache.reduceCached(this.left);
        Expression right = null;

        switch (operation) {
            case LOGICAL_AND:
                if (left instanceof ConstantBooleanExpression) {
                    if (!((ConstantBooleanExpression) left).value) {
                        return (ConstantBooleanExpression) left;
                    }
                    right = cache.reduceCached(this.right);
                    if (right instanceof ConstantBooleanExpression) {
                        return (ConstantBooleanExpression) right;
                    }
                }
                break;
            case LOGICAL_OR:
                if (left instanceof ConstantBooleanExpression) {
                    if (((ConstantBooleanExpression) left).value) {
                        return (ConstantBooleanExpression) left;
                    }
                    right = cache.reduceCached(this.right);
                    if (right instanceof ConstantBooleanExpression) {
                        return (ConstantBooleanExpression) right;
                    }
                }
                break;
            default:
        }

        if (right == null) {
            throw new RuntimeException("Operation " + operation + " is not implemented for "
                    + left.getClass().getSimpleName() + " as left hand side");
        } else {
            throw new RuntimeException("Operation " + operation + " is not implemented for "
                    + left.getClass().getSimpleName() + " and " + right.getClass().getSimpleName());
        }
    }

    public enum Operation {
        LOGICAL_AND(ChasmParser.LOGICAL_AND),
        LOGICAL_OR(ChasmParser.LOGICAL_OR),
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
