package org.quiltmc.chasm.lang.op;

import org.antlr.v4.runtime.tree.ParseTree;

public interface NumberLike extends AddableExpression, Subtractable, MultiplicableExpression, ComparableExpression {
    Expression negate(ParseTree tree);

    Expression invert(ParseTree tree);

    boolean canDivide(Expression expression);

    boolean canModulo(Expression expression);

    boolean canBitwiseAnd(Expression expression);

    boolean canBitwiseOr(Expression expression);

    boolean canBitwiseXor(Expression expression);

    boolean canLeftShift(Expression expression);

    boolean canRightShift(Expression expression);

    boolean canUnsignedRightShift(Expression expression);

    Expression divide(ParseTree tree, Expression expression);

    Expression modulo(ParseTree tree, Expression expression);

    Expression bitwiseAnd(ParseTree tree, Expression expression);

    Expression bitwiseOr(ParseTree tree, Expression expression);

    Expression bitwiseXor(ParseTree tree, Expression expression);

    Expression leftShift(ParseTree tree, Expression expression);

    Expression rightShift(ParseTree tree, Expression expression);

    Expression unsignedRightShift(ParseTree tree, Expression expression);
}
