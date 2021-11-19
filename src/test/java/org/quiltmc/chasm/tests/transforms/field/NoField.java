package org.quiltmc.chasm.tests.transforms.field;

import org.quiltmc.chasm.CheckTransformed;

@CheckTransformed(result = OneField.class, transformer = AddField.class)
public class NoField {
}
