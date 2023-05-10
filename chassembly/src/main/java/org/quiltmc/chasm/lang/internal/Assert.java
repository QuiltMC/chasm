package org.quiltmc.chasm.lang.internal;

import org.jetbrains.annotations.Contract;

public final class Assert {
    private Assert() {
        throw new AssertionError("No Assert instances for you!");
    }

    @Contract("false -> fail")
    public static void check(boolean value) {
        check(value, "Assertion failed");
    }

    @Contract("false, _ -> fail")
    public static void check(boolean value, String message) {
        if (!value) {
            throw new AssertionError(message);
        }
    }
}
