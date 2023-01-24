package org.quiltmc.chasm.lang.internal.eval;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;

import org.quiltmc.chasm.lang.api.ast.LambdaNode;
import org.quiltmc.chasm.lang.api.ast.MapNode;
import org.quiltmc.chasm.lang.api.ast.Node;
import org.quiltmc.chasm.lang.api.ast.ReferenceNode;
import org.quiltmc.chasm.lang.api.eval.Resolver;
import org.quiltmc.chasm.lang.api.exception.EvaluationException;

public class ResolverImpl implements Resolver {
    final ArrayList<Node> scopeStack = new ArrayList<>();

    final Map<ReferenceNode, Reference> references = new HashMap<>();

    final Map<LambdaNode, Map<String, Reference>> lambdaCaptures = new HashMap<>();

    public ResolverImpl(Node rootStack) {
        scopeStack.add(rootStack);
    }

    @Override
    public void resolveReference(ReferenceNode referenceNode) {
        if (references.containsKey(referenceNode)) {
            throw new EvaluationException("Reference node " + referenceNode + " has already been resolved");
        }

        Reference reference = resolveReference(referenceNode.getIdentifier(), referenceNode.isGlobal());

        if (reference == null) {
            throw new EvaluationException("Unresolved reference " + referenceNode);
        }

        references.put(referenceNode, reference);
    }

    private Reference resolveReference(String identifier, boolean global) {
        // Non-Global resolution starts at the newest entry and scans backwards
        ListIterator<Node> it = scopeStack.listIterator(global ? 0 : scopeStack.size());
        Reference reference = null;
        String globalIdentifier = (global ? "$" : "") + identifier;

        // Find first node resolving the reference
        while (global ? it.hasNext() : it.hasPrevious()) {
            Node node = global ? it.next() : it.previous();

            if (node instanceof MapNode) {
                Node target = ((MapNode) node).get(identifier);
                if (target != null) {
                    reference = new NodeReference(target);
                    break;
                }
            }

            if (node instanceof LambdaNode) {
                LambdaNode lambda = (LambdaNode) node;
                if (lambda.getIdentifier().equals(identifier)) {
                    reference = new LambdaReference(identifier, lambda);
                    break;
                }
            }
        }

        // Failed to resolve reference
        if (reference == null) {
            return null;
        }

        // Skip current node
        it.next();

        // Bubble reference up through lambda captures
        while (it.hasNext()) {
            Node node = it.next();

            if (node instanceof LambdaNode) {
                LambdaNode lambdaNode = (LambdaNode) node;
                Map<String, Reference> captures = lambdaCaptures.computeIfAbsent(lambdaNode, l -> new HashMap<>());
                captures.put(globalIdentifier, reference);
                reference = new LambdaReference(globalIdentifier, lambdaNode);
            }
        }

        return reference;
    }

    @Override
    public void enterMap(MapNode map) {
        scopeStack.add(map);
    }

    @Override
    public void exitMap() {
        scopeStack.remove(scopeStack.size() - 1);
    }

    @Override
    public void enterLambda(LambdaNode lambda) {
        scopeStack.add(lambda);
    }

    @Override
    public void exitLambda() {
        LambdaNode lambdaNode = (LambdaNode) scopeStack.remove(scopeStack.size() - 1);

        /*
        Map<String, Reference> captures = lambdaCaptures.get(lambdaNode);
        if (captures != null) {
            for (Map.Entry<String, Reference> entry : captures.entrySet()) {
                Reference reference = resolveReference(entry.getKey());
                if (reference == null) {
                    throw new EvaluationException("Failed to capture reference " + entry.getKey());
                }
                entry.setValue(reference);
            }
        }
        */
    }
}
