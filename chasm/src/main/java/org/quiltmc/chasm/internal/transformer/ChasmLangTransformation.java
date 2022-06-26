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
import org.quiltmc.chasm.api.tree.ListNode;
import org.quiltmc.chasm.api.tree.Node;
import org.quiltmc.chasm.api.tree.ValueNode;
import org.quiltmc.chasm.lang.Evaluator;
import org.quiltmc.chasm.lang.ast.AbstractMapExpression;
import org.quiltmc.chasm.lang.ast.IntegerExpression;
import org.quiltmc.chasm.lang.ast.LiteralExpression;
import org.quiltmc.chasm.lang.ast.SimpleMapExpression;
import org.quiltmc.chasm.internal.transformer.tree.NodeExpression;
import org.quiltmc.chasm.lang.op.Expression;
import org.quiltmc.chasm.lang.op.FunctionExpression;
import org.quiltmc.chasm.lang.op.ListExpression;

public class ChasmLangTransformation implements Transformation {
    private final Transformer parent;
    private final Evaluator evaluator;
    private final Expression transformation;
    private final Target target;
    private final Map<String, Target> sources = new LinkedHashMap<>();

    public ChasmLangTransformation(Transformer parent, Evaluator evaluator, Expression transformation) {
        this.parent = parent;
        this.evaluator = evaluator;
        this.transformation = transformation;

        Expression targetResolved = ((AbstractMapExpression) transformation).get("target");
        Expression targetReduced = evaluator.reduce(targetResolved);
        this.target = parseTarget(targetReduced);

        Expression sourcesResolved = ((AbstractMapExpression) transformation).get("sources");
        Expression sourcesReduced = evaluator.reduce(sourcesResolved);
        AbstractMapExpression sources = (AbstractMapExpression) sourcesReduced;
        if (sources != null) {
            for (String key : sources.getKeys()) {
                Expression sourceResolved = sources.get(key);
                Expression sourceReduced = evaluator.reduce(sourceResolved);
                Target source = parseTarget(sourceReduced);
                this.sources.put(key, source);
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
        HashMap<String, Expression> args = new HashMap<>();
        args.put("target", NodeExpression.from(null, targetNode));

        HashMap<String, Expression> sources = new HashMap<>();
        for (Map.Entry<String, Node> entry : nodeSources.entrySet()) {
            sources.put(entry.getKey(), NodeExpression.from(null, entry.getValue()));
        }
        args.put("sources", new SimpleMapExpression(null, sources));

        Expression applyResolved = ((AbstractMapExpression) transformation).get("apply");
        Expression applyReduced = evaluator.reduce(applyResolved);
        FunctionExpression apply = (FunctionExpression) applyReduced;

        Expression resolvedResult = apply.call(new SimpleMapExpression(null, args));
        Expression reducedResult = evaluator.reduceRecursive(resolvedResult);
        return parseNode(reducedResult);
    }

    private Target parseTarget(Expression expression) {
        AbstractMapExpression target = (AbstractMapExpression) expression;

        Expression nodeResolved = target.get("node");
        Expression nodeReduced = evaluator.reduce(nodeResolved);
        Node node = ((NodeExpression) nodeReduced).getNode();

        Integer start = null;
        Expression startResolved = target.get("start");
        if (startResolved != null) {
            Expression startReduced = evaluator.reduce(startResolved);
            start = ((IntegerExpression) startReduced).getValue();
        }

        Integer end = null;
        Expression endResolved = target.get("end");
        if (endResolved != null) {
            Expression endReduced = evaluator.reduce(endResolved);
            end = ((IntegerExpression) endReduced).getValue();
        }

        if (node instanceof ListNode && start != null && end != null) {
            return new SliceTarget((ListNode) node, start, end);
        } else {
            return new NodeTarget(node);
        }
    }

    private Node parseNode(Expression expression) {
        if (expression instanceof AbstractMapExpression) {
            LinkedHashMapNode mapNode = new LinkedHashMapNode();
            AbstractMapExpression mapExpression = (AbstractMapExpression) expression;
            for (String key : mapExpression.getKeys()) {
                mapNode.put(key, parseNode(mapExpression.get(key)));
            }
            return mapNode;
        } else if (expression instanceof ListExpression) {
            ArrayListNode listNode = new ArrayListNode();
            ListExpression listExpression = (ListExpression) expression;
            for (Expression entry : listExpression) {
                listNode.add(parseNode(entry));
            }
            return listNode;
        } else if (expression instanceof LiteralExpression) {
            LiteralExpression<?> literal = (LiteralExpression<?>) expression;
            return new ValueNode(literal.getValue());
        } else {
            throw new RuntimeException("Can't convert expression to chasm node: " + expression.getClass());
        }
    }
}
