package org.quiltmc.chasm.internal.transformer;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.quiltmc.chasm.api.Transformation;
import org.quiltmc.chasm.api.Transformer;
import org.quiltmc.chasm.api.target.NodeTarget;
import org.quiltmc.chasm.api.target.SliceTarget;
import org.quiltmc.chasm.api.target.Target;
import org.quiltmc.chasm.internal.util.NodeUtils;
import org.quiltmc.chasm.lang.api.ast.CallNode;
import org.quiltmc.chasm.lang.api.ast.IntegerNode;
import org.quiltmc.chasm.lang.api.ast.LambdaNode;
import org.quiltmc.chasm.lang.api.ast.ListNode;
import org.quiltmc.chasm.lang.api.ast.MapNode;
import org.quiltmc.chasm.lang.api.ast.Node;
import org.quiltmc.chasm.lang.api.eval.Evaluator;
import org.quiltmc.chasm.lang.api.eval.FunctionNode;

public class ChasmLangTransformation implements Transformation {
    private final Transformer parent;
    private final Evaluator evaluator;
    private final FunctionNode apply;
    private final Target target;
    private final Map<String, Target> sources = new LinkedHashMap<>();

    public ChasmLangTransformation(Transformer parent, Node node, Evaluator evaluator) {
        this.parent = parent;
        this.evaluator = evaluator;

        if (!(node instanceof MapNode)) {
            throw new RuntimeException("Transformations must be maps");
        }

        MapNode transformationExpression = (MapNode) node;

        Node targetNode = transformationExpression.getEntries().get("target");
        if (!(targetNode instanceof MapNode)) {
            throw new RuntimeException("Transformations must declare a map \"target\" in their root map");
        }
        this.target = parseTarget((MapNode) targetNode);

        Node applyNode = transformationExpression.getEntries().get("apply");
        if (!(applyNode instanceof FunctionNode)) {
            throw new RuntimeException("Transformations must declare a function \"apply\" in their root map");
        }
        this.apply = (FunctionNode) applyNode;

        Node sourcesNode = transformationExpression.getEntries().get("sources");
        if (sourcesNode != null) {
            if (!(sourcesNode instanceof MapNode)) {
                throw new RuntimeException("Element \"sources\" in transformation must be a map");
            }

            for (Map.Entry<String, Node> entry : ((MapNode) sourcesNode).getEntries().entrySet()) {
                if (!(entry.getValue() instanceof MapNode)) {
                    throw new RuntimeException("Transformation sources must be maps");
                }

                Target source = parseTarget((MapNode) entry.getValue());
                this.sources.put(entry.getKey(), source);
            }
        }
    }

    @Override
    public Transformer getParent() {
        return parent;
    }

    @Override
    public Target getTarget() {
        return target;
    }

    @Override
    public Map<String, Target> getSources() {
        return sources;
    }

    @Override
    public Node apply(Node targetNode, Map<String, Node> nodeSources) {
        HashMap<String, Node> args = new HashMap<>();
        args.put("target", targetNode);
        args.put("sources", new MapNode(nodeSources));

        CallNode callExpression = new CallNode(apply, new MapNode(args));
        return callExpression.evaluate(evaluator);
    }

    private Target parseTarget(MapNode target) {
        Node targetNode = NodeUtils.get(target, "node");

        Integer start = null;
        Node startNode = NodeUtils.get(target, "start");
        if (startNode instanceof IntegerNode) {
            start = ((IntegerNode) startNode).getValue().intValue();
        }

        Integer end = null;
        Node endNode = NodeUtils.get(target, "end");
        if (endNode instanceof IntegerNode) {
            end = ((IntegerNode) endNode).getValue().intValue();
        }

        if (targetNode instanceof ListNode && start != null && end != null) {
            return new SliceTarget((ListNode) targetNode, start, end);
        } else {
            return new NodeTarget(targetNode);
        }
    }
}
