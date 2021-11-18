package org.quiltmc.chasm.testclasses;

public sealed class SealedTest permits SealedTest.SealedExtendsTest {
    public static final class SealedExtendsTest extends SealedTest {

    }
}
