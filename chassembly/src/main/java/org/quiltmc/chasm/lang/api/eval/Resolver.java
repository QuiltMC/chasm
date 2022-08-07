package org.quiltmc.chasm.lang.api.eval;

//import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.chasm.lang.api.ast.LambdaNode;
import org.quiltmc.chasm.lang.api.ast.MapNode;
import org.quiltmc.chasm.lang.api.ast.ReferenceNode;

//@ApiStatus.NonExtendable
public interface Resolver {
    void resolveReference(ReferenceNode reference);

    void enterMap(MapNode map);

    void exitMap();

    void enterLambda(LambdaNode lambda);

    void exitLambda();
}
