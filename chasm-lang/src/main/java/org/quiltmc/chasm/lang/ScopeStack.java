package org.quiltmc.chasm.lang;

import java.util.ArrayDeque;
import java.util.Iterator;

import org.quiltmc.chasm.lang.op.Expression;

public class ScopeStack {
    private final ArrayDeque<Scope> stack;

    public ScopeStack() {
        stack = new ArrayDeque<>();
    }

    private ScopeStack(ScopeStack other) {
        stack = other.stack.clone();
    }

    public ScopeStack copy() {
        return new ScopeStack(this);
    }

    public void push(Scope scope) {
        stack.push(scope);
    }

    public void pop() {
        stack.pop();
    }

    public boolean contains(String identifier) {
        return get(identifier) != null;
    }

    public Expression get(String identifier) {
        Iterator<Scope> iterator;
        if (identifier.startsWith("$")) {
            // Walk backwards for reverse references
            iterator = stack.descendingIterator();
            // Also strip the leading $
            identifier = identifier.substring(1);
        } else {
            // Walk forwards for forward references
            iterator = stack.iterator();
        }

        while (iterator.hasNext()) {
            Scope scope = iterator.next();
            if (scope.contains(identifier)) {
                return scope.get(identifier);
            }
        }

        return null;
    }
}
