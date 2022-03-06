package org.quiltmc.chasm.lang.ast;

import org.quiltmc.chasm.lang.op.NumberLike;

public class IntegerExpression extends LiteralExpression<Integer> implements NumberLike {
    public IntegerExpression(int value) {
        super(value);
    }

    @Override
    public IntegerExpression copy() {
        return new IntegerExpression(value);
    }

    @Override
    public boolean canAdd(Expression expression) {
        return expression instanceof IntegerExpression;
    }

    @Override
    public Expression add(Expression expression) {
        int result = value + ((IntegerExpression) expression).value;
        return new IntegerExpression(result);
    }

    @Override
    public boolean canSubtract(Expression expression) {
        return expression instanceof IntegerExpression;
    }

    @Override
    public Expression subtract(Expression expression) {
        int result = value - ((IntegerExpression) expression).value;
        return new IntegerExpression(result);
    }

    @Override
    public boolean canMultiply(Expression expression) {
        return expression instanceof IntegerExpression;
    }

    @Override
    public Expression multiply(Expression expression) {
        int result = value * ((IntegerExpression) expression).value;
        return new IntegerExpression(result);
    }

    @Override
    public Expression negate() {
        return new IntegerExpression(-value);
    }

    @Override
    public Expression invert() {
        return new IntegerExpression(~value);
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
    public boolean canBitwiseSHL(Expression expression) {
        return expression instanceof IntegerExpression;
    }

    @Override
    public boolean canBitwiseSHR(Expression expression) {
        return expression instanceof IntegerExpression;
    }

    @Override
    public boolean canBitwiseUSHR(Expression expression) {
        return expression instanceof IntegerExpression;
    }

    @Override
    public Expression divide(Expression expression) {
        int result = value / ((IntegerExpression) expression).value;
        return new IntegerExpression(result);
    }

    @Override
    public Expression modulo(Expression expression) {
        int result = value % ((IntegerExpression) expression).value;
        return new IntegerExpression(result);
    }

    @Override
    public Expression bitwiseAnd(Expression expression) {
        int result = value & ((IntegerExpression) expression).value;
        return new IntegerExpression(result);
    }

    @Override
    public Expression bitwiseOr(Expression expression) {
        int result = value | ((IntegerExpression) expression).value;
        return new IntegerExpression(result);
    }

    @Override
    public Expression bitwiseXor(Expression expression) {
        int result = value ^ ((IntegerExpression) expression).value;
        return new IntegerExpression(result);
    }

    @Override
    public Expression bitwiseSHL(Expression expression) {
        int result = value << ((IntegerExpression) expression).value;
        return new IntegerExpression(result);
    }

    @Override
    public Expression bitwiseSHR(Expression expression) {
        int result = value >> ((IntegerExpression) expression).value;
        return new IntegerExpression(result);
    }

    @Override
    public Expression bitwiseUSHR(Expression expression) {
        int result = value >>> ((IntegerExpression) expression).value;
        return new IntegerExpression(result);
    }

    @Override
    public boolean canCompare(Expression expression) {
        return expression instanceof IntegerExpression;
    }

    @Override
    public Expression lessThan(Expression expression) {
        return new ConstantBooleanExpression(value < ((IntegerExpression) expression).value);
    }

    @Override
    public Expression lessThanOrEqual(Expression expression) {
        return new ConstantBooleanExpression(value <= ((IntegerExpression) expression).value);
    }

    @Override
    public Expression greaterThan(Expression expression) {
        return new ConstantBooleanExpression(value > ((IntegerExpression) expression).value);
    }

    @Override
    public Expression greaterThanOrEqual(Expression expression) {
        return new ConstantBooleanExpression(value >= ((IntegerExpression) expression).value);
    }
}
