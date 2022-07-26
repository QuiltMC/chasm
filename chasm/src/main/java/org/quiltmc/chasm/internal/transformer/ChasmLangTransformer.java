package org.quiltmc.chasm.internal.transformer;

import java.util.ArrayList;
import java.util.Collection;

import org.quiltmc.chasm.api.Transformation;
import org.quiltmc.chasm.api.Transformer;
import org.quiltmc.chasm.lang.api.ast.CallNode;
import org.quiltmc.chasm.lang.api.ast.LambdaNode;
import org.quiltmc.chasm.lang.api.ast.ListNode;
import org.quiltmc.chasm.lang.api.ast.MapNode;
import org.quiltmc.chasm.lang.api.ast.Node;
import org.quiltmc.chasm.lang.api.eval.Evaluator;

public class ChasmLangTransformer implements Transformer {
    private final Node parsed;

    private final String id;

    public ChasmLangTransformer(String id, Node parsed) {
        this.id = id;
        this.parsed = parsed;
    }

    @Override
    public Collection<Transformation> apply(ListNode classes) {
        LambdaNode lambdaExpression = new LambdaNode("classes", parsed);
        CallNode callExpression = new CallNode(lambdaExpression, classes);

        Evaluator evaluator = Evaluator.create(callExpression);
        Node evaluated = callExpression.evaluate(evaluator);
        if (!(evaluated instanceof MapNode)) {
            throw new RuntimeException("Transformers must be maps");
        }

        MapNode transformerExpression = (MapNode) evaluated;
        Node transformationsNode = transformerExpression.getEntries().get("transformations");
        if (!(transformationsNode instanceof ListNode)) {
            throw new RuntimeException("Transformers must declare a list \"transformations\" in their root map");
        }

        ArrayList<Transformation> transformations = new ArrayList<>();
        for (Node entry : ((ListNode) transformationsNode).getEntries()) {
            transformations.add(new ChasmLangTransformation(this, entry, evaluator));
        }

        return transformations;
    }

    @Override
    public String getId() {
        return id;
    }
}
