package org.quiltmc.chasm.lang.api.ast;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ListExpression extends Expression {
    private List<Expression> entries;

    public ListExpression(List<Expression> entries) {
        this.entries = entries;
    }

    public List<Expression> getEntries() {
        return entries;
    }

    @Override
    public ListExpression copy() {
        List<Expression> newEntries = new ArrayList<>();

        for (Expression entry : entries) {
            newEntries.add(entry.copy());
        }

        return new ListExpression(newEntries);
    }
}
