package org.quiltmc.chasm.tests.unchanged.empty;

import org.quiltmc.chasm.CheckUnchanged;

@CheckUnchanged
public class EmptyOuterClass {
    @CheckUnchanged
    static class EmptyStaticNestedClass {
    }

    @CheckUnchanged
    class EmptyInnerClass {
    }
}
