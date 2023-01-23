package org.quiltmc.chasm.lang.internal;

public final class Assert {
    private Assert() {
        throw new AssertionError("No Assert instances for you!");
    }

    public static void check(boolean value) {
        check(value, "Assertion failed");
    }

    public static void check(boolean value, String message) {
        if (!value) {
            throw new AssertionError(message);
        }
    }
}
