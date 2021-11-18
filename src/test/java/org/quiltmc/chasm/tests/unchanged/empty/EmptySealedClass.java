package org.quiltmc.chasm.tests.unchanged.empty;

import org.quiltmc.chasm.CheckUnchanged;

@CheckUnchanged
public sealed class EmptySealedClass permits EmptySealedExtendsClass {
}

@CheckUnchanged
final class EmptySealedExtendsClass extends EmptySealedClass {
}
