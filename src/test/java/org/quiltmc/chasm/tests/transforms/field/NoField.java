package org.quiltmc.chasm.tests.transforms.field;

import org.quiltmc.chasm.CheckTransformed;

@CheckTransformed(expected = "AddFieldToNoField.result", transformer = AddField.class)
public class NoField {
}
