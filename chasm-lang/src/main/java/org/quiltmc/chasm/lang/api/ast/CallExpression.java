package org.quiltmc.chasm.lang.api.ast;

import java.util.Objects;

public class CallExpression extends Expression {
    private Expression function;
    private Expression arg;

    public CallExpression(Expression function, Expression arg) {
        this.function = function;
        this.arg = arg;
    }

    public Expression getFunction() {
        return function;
    }

    public void setFunction(Expression function) {
        this.function = function;
    }

    public Expression getArg() {
        return arg;
    }

    public void setArg(Expression arg) {
        this.arg = arg;
    }

    @Override
    public CallExpression copy() {
        return new CallExpression(function.copy(), arg.copy());
    }
}
