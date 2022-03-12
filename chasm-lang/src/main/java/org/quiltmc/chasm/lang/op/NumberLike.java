package org.quiltmc.chasm.lang.op;

import org.quiltmc.chasm.lang.ast.Expression;

public interface NumberLike extends Addable, Subtractable, Multiplicable, Comparable {
    Expression negate();

    Expression invert();

    boolean canDivide(Expression expression);

    boolean canModulo(Expression expression);

    boolean canBitwiseAnd(Expression expression);

    boolean canBitwiseOr(Expression expression);

    boolean canBitwiseXor(Expression expression);

    boolean canLeftShift(Expression expression);

    boolean canRightShift(Expression expression);

    boolean canUnsignedRightShift(Expression expression);

    Expression divide(Expression expression);

    Expression modulo(Expression expression);

    Expression bitwiseAnd(Expression expression);

    Expression bitwiseOr(Expression expression);

    Expression bitwiseXor(Expression expression);

    Expression leftShift(Expression expression);

    Expression rightShift(Expression expression);

    Expression unsignedRightShift(Expression expression);
}
