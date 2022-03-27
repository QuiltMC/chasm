package org.quiltmc.chasm.lang;

import java.util.Map;

import org.quiltmc.chasm.lang.op.Expression;

public interface Scope {
    boolean contains(String identifier);

    Expression get(String identifier);

    static Scope singleton(String identifier, Expression value) {
        return new Scope() {
            @Override
            public boolean contains(String id) {
                return identifier.equals(id);
            }

            @Override
            public Expression get(String id) {
                return identifier.equals(id) ? value : null;
            }
        };
    }

    static Scope map(Map<String, Expression> map) {
        return new Scope() {
            @Override
            public boolean contains(String identifier) {
                return map.containsKey(identifier);
            }

            @Override
            public Expression get(String identifier) {
                return map.get(identifier);
            }
        };
    }
}
