package org.quiltmc.chasm.tests;

import org.quiltmc.chasm.CheckUnchanged;

@CheckUnchanged
public sealed class SealedTest permits SealedTest.SealedExtendsTest {
    @CheckUnchanged
    public static final class SealedExtendsTest extends SealedTest {
    }
}
