package org.quiltmc.chasm.internal.transformer;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.quiltmc.chasm.api.Transformation;
import org.quiltmc.chasm.api.Transformer;
import org.quiltmc.chasm.api.target.NodeTarget;
import org.quiltmc.chasm.api.target.SliceTarget;
import org.quiltmc.chasm.api.target.Target;
import org.quiltmc.chasm.api.tree.ArrayListNode;
import org.quiltmc.chasm.api.tree.LinkedHashMapNode;
import org.quiltmc.chasm.api.tree.ValueNode;
import org.quiltmc.chasm.internal.transformer.tree.NodeNode;
import org.quiltmc.chasm.lang.api.ast.CallNode;
import org.quiltmc.chasm.lang.api.ast.Node;
import org.quiltmc.chasm.lang.api.ast.LambdaNode;
import org.quiltmc.chasm.lang.api.ast.ListNode;
import org.quiltmc.chasm.lang.api.ast.MapNode;
import org.quiltmc.chasm.lang.api.eval.Evaluator;

public class ChasmLangTransformation implements Transformation {
    private final Transformer parent;
    private final Evaluator evaluator;
    private final LambdaNode apply;
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
        if (!(applyNode instanceof LambdaNode)) {
            throw new RuntimeException("Transformations must declare a function \"apply\" in their root map");
        }
        this.apply = (LambdaNode) applyNode;

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
    public org.quiltmc.chasm.api.tree.Node apply(org.quiltmc.chasm.api.tree.Node targetNode, Map<String, org.quiltmc.chasm.api.tree.Node> nodeSources) {
        HashMap<String, Node> args = new HashMap<>();
        args.put("target", NodeNode.from(null, targetNode));

        HashMap<String, Node> sources = new HashMap<>();
        for (Map.Entry<String, org.quiltmc.chasm.api.tree.Node> entry : nodeSources.entrySet()) {
            sources.put(entry.getKey(), NodeNode.from(null, entry.getValue()));
        }
        args.put("sources", new MapNode(sources));

        CallNode callExpression = new CallNode(apply, new MapNode(args));
        Node result = evaluator.evaluate(callExpression);

        return parseNode(result);
    }

    private Target parseTarget(Node expression) {
        AbstractMapExpression target = (AbstractMapExpression) expression;

        Node nodeResolved = target.get("node");
        Node nodeReduced = evaluator.reduce(nodeResolved);
        org.quiltmc.chasm.api.tree.Node node = ((NodeNode) nodeReduced).getNode();

        Integer start = null;
        Node startResolved = target.get("start");
        if (startResolved != null) {
            Node startReduced = evaluator.reduce(startResolved);
            start = ((IntegerExpression) startReduced).getValue();
        }

        Integer end = null;
        Node endResolved = target.get("end");
        if (endResolved != null) {
            Node endReduced = evaluator.reduce(endResolved);
            end = ((IntegerExpression) endReduced).getValue();
        }

        if (node instanceof org.quiltmc.chasm.api.tree.ListNode && start != null && end != null) {
            return new SliceTarget((org.quiltmc.chasm.api.tree.ListNode) node, start, end);
        } else {
            return new NodeTarget(node);
        }
    }

    private org.quiltmc.chasm.api.tree.Node parseNode(Node node) {
        if (node instanceof AbstractMapExpression) {
            LinkedHashMapNode mapNode = new LinkedHashMapNode();
            AbstractMapExpression mapExpression = (AbstractMapExpression) node;
            for (String key : mapExpression.getKeys()) {
                mapNode.put(key, parseNode(mapExpression.get(key)));
            }
            return mapNode;
        } else if (node instanceof ListNode) {
            ArrayListNode listNode = new ArrayListNode();
            ListNode listExpression = (ListNode) node;
            for (Node entry : listExpression) {
                listNode.add(parseNode(entry));
            }
            return listNode;
        } else if (node instanceof LiteralExpression) {
            LiteralExpression<?> literal = (LiteralExpression<?>) node;
            return new ValueNode(literal.getValue());
        } else {
            throw new RuntimeException("Can't convert expression to chasm node: " + node.getClass());
        }
    }
}
