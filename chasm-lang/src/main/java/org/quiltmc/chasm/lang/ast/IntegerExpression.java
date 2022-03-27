package org.quiltmc.chasm.lang.ast;

import org.antlr.v4.runtime.tree.ParseTree;
import org.quiltmc.chasm.lang.op.Expression;
import org.quiltmc.chasm.lang.op.NumberLike;

public class IntegerExpression extends LiteralExpression<Integer> implements NumberLike {
    public IntegerExpression(ParseTree tree, int value) {
        super(tree, value);
    }

    @Override
    public boolean canAdd(Expression expression) {
        return expression instanceof IntegerExpression;
    }

    @Override
    public Expression add(ParseTree tree, Expression expression) {
        int result = value + ((IntegerExpression) expression).value;
        return new IntegerExpression(tree, result);
    }

    @Override
    public boolean canSubtract(Expression expression) {
        return expression instanceof IntegerExpression;
    }

    @Override
    public Expression subtract(ParseTree tree, Expression expression) {
        int result = value - ((IntegerExpression) expression).value;
        return new IntegerExpression(tree, result);
    }

    @Override
    public boolean canMultiply(Expression expression) {
        return expression instanceof IntegerExpression;
    }

    @Override
    public Expression multiply(ParseTree tree, Expression expression) {
        int result = value * ((IntegerExpression) expression).value;
        return new IntegerExpression(tree, result);
    }

    @Override
    public Expression negate(ParseTree tree) {
        return new IntegerExpression(tree, -value);
    }

    @Override
    public Expression invert(ParseTree tree) {
        return new IntegerExpression(tree, ~value);
    }

    @Override
    public boolean canDivide(Expression expression) {
        return expression instanceof IntegerExpression;
    }

    @Override
    public boolean canModulo(Expression expression) {
        return expression instanceof IntegerExpression;
    }

    @Override
    public boolean canBitwiseAnd(Expression expression) {
        return expression instanceof IntegerExpression;
    }

    @Override
    public boolean canBitwiseOr(Expression expression) {
        return expression instanceof IntegerExpression;
    }

    @Override
    public boolean canBitwiseXor(Expression expression) {
        return expression instanceof IntegerExpression;
    }

    @Override
    public boolean canLeftShift(Expression expression) {
        return expression instanceof IntegerExpression;
    }

    @Override
    public boolean canRightShift(Expression expression) {
        return expression instanceof IntegerExpression;
    }

    @Override
    public boolean canUnsignedRightShift(Expression expression) {
        return expression instanceof IntegerExpression;
    }

    @Override
    public Expression divide(ParseTree tree, Expression expression) {
        int result = value / ((IntegerExpression) expression).value;
        return new IntegerExpression(tree, result);
    }

    @Override
    public Expression modulo(ParseTree tree, Expression expression) {
        int result = value % ((IntegerExpression) expression).value;
        return new IntegerExpression(tree, result);
    }

    @Override
    public Expression bitwiseAnd(ParseTree tree, Expression expression) {
        int result = value & ((IntegerExpression) expression).value;
        return new IntegerExpression(tree, result);
    }

    @Override
    public Expression bitwiseOr(ParseTree tree, Expression expression) {
        int result = value | ((IntegerExpression) expression).value;
        return new IntegerExpression(tree, result);
    }

    @Override
    public Expression bitwiseXor(ParseTree tree, Expression expression) {
        int result = value ^ ((IntegerExpression) expression).value;
        return new IntegerExpression(tree, result);
    }

    @Override
    public Expression leftShift(ParseTree tree, Expression expression) {
        int result = value << ((IntegerExpression) expression).value;
        return new IntegerExpression(tree, result);
    }

    @Override
    public Expression rightShift(ParseTree tree, Expression expression) {
        int result = value >> ((IntegerExpression) expression).value;
        return new IntegerExpression(tree, result);
    }

    @Override
    public Expression unsignedRightShift(ParseTree tree, Expression expression) {
        int result = value >>> ((IntegerExpression) expression).value;
        return new IntegerExpression(tree, result);
    }

    @Override
    public boolean canCompare(Expression expression) {
        return expression instanceof IntegerExpression;
    }

    @Override
    public Expression lessThan(ParseTree tree, Expression expression) {
        return new ConstantBooleanExpression(tree, value < ((IntegerExpression) expression).value);
    }

    @Override
    public Expression lessThanOrEqual(ParseTree tree, Expression expression) {
        return new ConstantBooleanExpression(tree, value <= ((IntegerExpression) expression).value);
    }

    @Override
    public Expression greaterThan(ParseTree tree, Expression expression) {
        return new ConstantBooleanExpression(tree, value > ((IntegerExpression) expression).value);
    }

    @Override
    public Expression greaterThanOrEqual(ParseTree tree, Expression expression) {
        return new ConstantBooleanExpression(tree, value >= ((IntegerExpression) expression).value);
    }
}
