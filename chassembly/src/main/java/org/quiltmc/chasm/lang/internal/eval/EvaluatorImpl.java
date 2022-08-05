package org.quiltmc.chasm.lang.internal.eval;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.quiltmc.chasm.lang.api.ast.LambdaNode;
import org.quiltmc.chasm.lang.api.ast.MapNode;
import org.quiltmc.chasm.lang.api.ast.Node;
import org.quiltmc.chasm.lang.api.ast.ReferenceNode;
import org.quiltmc.chasm.lang.api.eval.ClosureNode;
import org.quiltmc.chasm.lang.api.eval.Evaluator;
import org.quiltmc.chasm.lang.api.exception.EvaluationException;
import org.quiltmc.chasm.lang.internal.intrinsics.Intrinsics;
import org.quiltmc.chasm.lang.internal.render.Renderer;

public class EvaluatorImpl implements Evaluator {
    private static final Renderer traceEntryRenderer = Renderer.builder().trailingCommas(false).build();

    ResolverImpl resolver = new ResolverImpl(new MapNode(Intrinsics.ALL));

    ArrayDeque<CallStackEntry> callStack = new ArrayDeque<>();

    static class CallStackEntry {
        final LambdaNode lambda;
        final Map<String, Node> scope;

        CallStackEntry(LambdaNode lambda, Map<String, Node> scope) {
            this.lambda = lambda;
            this.scope = scope;
        }
    }

    HashMap<ClosureCacheKey, ClosureNode> closureCache = new HashMap<>();

    static class ClosureCacheKey {
        private final LambdaNode lambdaNode;
        private final ArrayList<CallStackEntry> callStack;

        ClosureCacheKey(LambdaNode lambdaNode, ArrayDeque<CallStackEntry> callStack) {
            this.lambdaNode = lambdaNode;
            this.callStack = new ArrayList<>(callStack);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ClosureCacheKey that = (ClosureCacheKey) o;
            return Objects.equals(lambdaNode, that.lambdaNode) && Objects.equals(callStack, that.callStack);
        }

        @Override
        public int hashCode() {
            return Objects.hash(lambdaNode, callStack);
        }
    }

    ArrayDeque<TraceStackEntry> traceStack = new ArrayDeque<>();

    static class TraceStackEntry {
        final Node beingEvaluated;
        final String entry;

        TraceStackEntry(Node beingEvaluated, String entry) {
            this.beingEvaluated = beingEvaluated;
            this.entry = entry;
        }
    }

    public EvaluatorImpl(Node node) {
        node.resolve(resolver);
    }

    @Override
    public Node resolveReference(ReferenceNode reference) {
        return resolveReference(resolver.references.get(reference));
    }

    private Node resolveReference(Reference reference) {
        if (reference instanceof NodeReference) {
            return ((NodeReference) reference).getNode();
        }

        if (reference instanceof LambdaReference) {
            CallStackEntry topEntry = callStack.peek();
            assert Objects.requireNonNull(topEntry).lambda == ((LambdaReference) reference).getLambda();
            return topEntry.scope.get(((LambdaReference) reference).getIdentifier());
        }

        throw new EvaluationException("Failed to resolve reference: " + reference);
    }

    @Override
    public ClosureNode createClosure(LambdaNode lambdaNode) {
        ClosureCacheKey cacheKey = new ClosureCacheKey(lambdaNode, callStack);
        if (closureCache.containsKey(cacheKey)) {
            return closureCache.get(cacheKey);
        }


        Map<String, Node> resolvedReferences = new HashMap<>();
        ClosureNode closure = new ClosureNode(lambdaNode, resolvedReferences);
        closureCache.put(cacheKey, closure);

        Map<String, Reference> captures = resolver.lambdaCaptures.get(lambdaNode);
        if (captures != null) {
            for (Map.Entry<String, Reference> entry : captures.entrySet()) {
                resolvedReferences.put(entry.getKey(), resolveReference(entry.getValue()).evaluate(this));
            }
        }

        return closure;
    }

    @Override
    public Node callClosure(ClosureNode closure, Node arg) {
        HashMap<String, Node> scope = new HashMap<>(closure.getCaptures());
        scope.put(closure.getLambda().getIdentifier(), arg);

        callStack.push(new CallStackEntry(closure.getLambda(), scope));
        Node result = closure.getLambda().getInner().evaluate(this);
        callStack.pop();

        return result;
    }

    @Override
    public void pushTrace(Node beingEvaluated, String traceEntry) {
        this.traceStack.addLast(new TraceStackEntry(beingEvaluated, traceEntry));
    }

    @Override
    public void popTrace() {
        this.traceStack.removeLast();
    }

    @Override
    public String renderTrace() {
        String nodeBeingEvaluated = "<Last traceback entry not found>";
        TraceStackEntry topEntry = traceStack.peekLast();
        if (topEntry != null) {
            nodeBeingEvaluated = traceEntryRenderer.render(topEntry.beingEvaluated)
                .replaceAll("\n", ""); // Remove trailing newline at the end of render
        }

        String traceback = this.traceStack.stream()
                .map(entry -> entry.entry)
                .reduce((a, b) -> a + " > " + b)
                .orElse("<Unable to render traceback>");

        return "SOURCE: <" + nodeBeingEvaluated + ">\n AT: " + traceback;
    }
}
