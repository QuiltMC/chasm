package org.quiltmc.chasm.lang.api.eval;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.chasm.lang.api.ast.LambdaNode;
import org.quiltmc.chasm.lang.api.ast.MapNode;
import org.quiltmc.chasm.lang.api.ast.ReferenceNode;

/**
 * A helper for resolving which node chassembly references refer to.
 */
@ApiStatus.NonExtendable
public interface Resolver {
    /**
     * Resolves the reference and stores the result in this resolver, so that it can be queried later by the evaluator.
     */
    void resolveReference(ReferenceNode reference);

    /**
     * Enters a map scope.
     */
    void enterMap(MapNode map);

    /**
     * Exits a map scope.
     */
    void exitMap();

    /**
     * Enters a lambda scope.
     */
    void enterLambda(LambdaNode lambda);

    /**
     * Exits a lambda scope.
     */
    void exitLambda();
}
