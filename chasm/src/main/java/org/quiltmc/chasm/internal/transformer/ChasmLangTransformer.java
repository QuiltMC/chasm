package org.quiltmc.chasm.internal.transformer;

import java.util.ArrayList;
import java.util.Collection;

import org.quiltmc.chasm.api.Transformation;
import org.quiltmc.chasm.api.Transformer;
import org.quiltmc.chasm.internal.transformer.tree.NodeNode;
import org.quiltmc.chasm.lang.api.ast.CallNode;
import org.quiltmc.chasm.lang.api.ast.Node;
import org.quiltmc.chasm.lang.api.ast.LambdaNode;
import org.quiltmc.chasm.lang.api.ast.ListNode;
import org.quiltmc.chasm.lang.api.ast.MapNode;
import org.quiltmc.chasm.lang.api.eval.Evaluator;

public class ChasmLangTransformer implements Transformer {
    private final Evaluator evaluator;
    private final Node parsed;

    private final String id;

    public ChasmLangTransformer(String id, Node parsed, Evaluator evaluator) {
        this.id = id;
        this.evaluator = evaluator;
        this.parsed = parsed;
    }

    @Override
    public Collection<Transformation> apply(org.quiltmc.chasm.api.tree.ListNode classes) {
        Node classesNode = NodeNode.from(null, classes);

        LambdaNode lambdaExpression = new LambdaNode("classes", parsed);
        CallNode callExpression = new CallNode(lambdaExpression, classesNode);

        Node evaluated = evaluator.evaluate(callExpression);
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
