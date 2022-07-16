package org.quiltmc.chasm.lang.internal.eval;

import java.util.Collections;
import java.util.Map;

import org.quiltmc.chasm.lang.api.ast.Expression;

public class Scope {
    private final Scope parent;
    private final Map<String, Expression> entries;

    public Scope() {
        this(null, Collections.emptyMap());
    }

    public Scope(Scope parent, Map<String, Expression> entries) {
        this.parent = parent;
        this.entries = entries;
    }

    public Expression resolve(String id, boolean global) {
        Expression resolved;

        if (global && parent != null) {
            resolved = parent.resolve(id, true);
            if (resolved != null) {
                return resolved;
            }
        }

        resolved = entries.get(id);
        if (resolved != null) {
            return resolved;
        }

        if (!global && parent != null) {
            resolved = parent.resolve(id, false);
            if (resolved != null) {
                return resolved;
            }
        }

        return null;
    }
}
