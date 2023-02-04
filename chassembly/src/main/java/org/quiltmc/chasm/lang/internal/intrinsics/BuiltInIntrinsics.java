package org.quiltmc.chasm.lang.internal.intrinsics;

import java.util.HashMap;
import java.util.Map;

import org.quiltmc.chasm.lang.api.ast.Node;
import org.quiltmc.chasm.lang.api.eval.IntrinsicFunction;

public class BuiltInIntrinsics {
    public static final Map<String, Node> ALL = new HashMap<>();

    static {
        register(new CharsFunction());
        register(new JoinFunction());
        register(new LenFunction());
        register(new EntriesFunction());
        register(new FromEntriesFunction());
        register(new WithElementsFunction());
        register(new MapFunction());
        register(new ReduceFunction());
        register(new FlattenFunction());
        register(new ToIntegerFunction());
        register(new ToFloatFunction());
        register(new SplitFloatFunction());
    }

    private static void register(IntrinsicFunction function) {
        ALL.put(function.getName(), function);
    }
}
