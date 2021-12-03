package org.quiltmc.chasm.lang.ast;

import org.objectweb.asm.Type;

public class TypeExpression extends LiteralExpression<Type> {
    public TypeExpression(Type value) {
        super(value);
    }

    @Override
    public Expression copy() {
        return new TypeExpression(value);
    }
}
