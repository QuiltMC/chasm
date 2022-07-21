package org.quiltmc.chasm.lang.internal.intrinsics;

import java.util.HashMap;
import java.util.Map;

import org.quiltmc.chasm.lang.api.ast.Node;

public class Intrinsics {
    public static final Map<String, Node> ALL = new HashMap<>();

    static {
        register(new CharsFunction());
        register(new JoinFunction());
    }

    static void register(IntrinsicFunction function) {
        ALL.put(function.getName(), function);
    }
}
