package org.quiltmc.chasm.internal.transformer;

import java.util.ArrayList;
import java.util.Collection;

import org.quiltmc.chasm.api.Transformation;
import org.quiltmc.chasm.api.Transformer;
import org.quiltmc.chasm.api.tree.ListNode;
import org.quiltmc.chasm.internal.transformer.tree.NodeExpression;
import org.quiltmc.chasm.lang.Evaluator;
import org.quiltmc.chasm.lang.Scope;
import org.quiltmc.chasm.lang.ast.AbstractMapExpression;
import org.quiltmc.chasm.lang.ast.StringExpression;
import org.quiltmc.chasm.lang.op.Expression;
import org.quiltmc.chasm.lang.op.ListExpression;

public class ChasmLangTransformer implements Transformer {
    private final Evaluator evaluator;
    private final Expression parsed;

    private final String id;

    public ChasmLangTransformer(Evaluator evaluator, Expression parsed) {
        this.evaluator = evaluator;
        this.parsed = parsed;

        Expression resolved = evaluator.resolve(parsed);
        Expression reduced = evaluator.reduce(resolved);
        Expression idResolved = ((AbstractMapExpression) reduced).get("id");
        Expression idReduced = evaluator.reduce(idResolved);
        this.id = ((StringExpression) idReduced).getValue();
    }

    @Override
    public Collection<Transformation> apply(ListNode classes) {
        Expression classesExpression = NodeExpression.from(null, classes);

        evaluator.getScope().push(Scope.singleton("classes", classesExpression));
        Expression resolved = evaluator.resolve(parsed);
        evaluator.getScope().pop();

        Expression reduced = evaluator.reduce(resolved);
        Expression transformationsResolved = ((AbstractMapExpression) reduced).get("transformations");
        Expression transformationsReduced = evaluator.reduce(transformationsResolved);

        ArrayList<Transformation> transformations = new ArrayList<>();
        for (Expression transformation : (ListExpression) transformationsReduced) {
            transformations.add(new ChasmLangTransformation(this, evaluator, transformation));
        }

        return transformations;
    }

    @Override
    public String getId() {
        return id;
    }
}
