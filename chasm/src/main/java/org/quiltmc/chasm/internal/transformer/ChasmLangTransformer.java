package org.quiltmc.chasm.internal.transformer;

import java.util.ArrayList;
import java.util.Collection;

import org.quiltmc.chasm.api.Transformation;
import org.quiltmc.chasm.api.Transformer;
import org.quiltmc.chasm.api.util.Context;
import org.quiltmc.chasm.internal.intrinsic.ChasmIntrinsics;
import org.quiltmc.chasm.lang.api.ast.Ast;
import org.quiltmc.chasm.lang.api.ast.CallNode;
import org.quiltmc.chasm.lang.api.ast.LambdaNode;
import org.quiltmc.chasm.lang.api.ast.ListNode;
import org.quiltmc.chasm.lang.api.ast.MapNode;
import org.quiltmc.chasm.lang.api.ast.Node;
import org.quiltmc.chasm.lang.api.eval.Evaluator;

public class ChasmLangTransformer implements Transformer {
    private final Node parsed;

    private final String id;

    private final Context context;

    public ChasmLangTransformer(String id, Node parsed, Context context) {
        this.id = id;
        this.parsed = parsed;
        this.context = context;
    }

    @Override
    public Collection<Transformation> apply(ListNode classes) {
        LambdaNode lambdaExpression = Ast.lambda("classes", parsed);
        CallNode callExpression = Ast.call(lambdaExpression, classes);

        Evaluator evaluator = ChasmIntrinsics.makeEvaluator(callExpression, context);
        Node evaluated = callExpression.evaluate(evaluator);
        if (!(evaluated instanceof MapNode)) {
            throw new RuntimeException("Transformers must be maps");
        }

        MapNode transformerExpression = (MapNode) evaluated;
        Node transformationsNode = transformerExpression.get("transformations");
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
