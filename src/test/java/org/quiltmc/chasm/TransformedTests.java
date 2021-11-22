package org.quiltmc.chasm;

import org.quiltmc.chasm.transformer.field.AddField;

/**
 * Tests for checking transformations.
 */
public class TransformedTests extends TestsBase {
    @Override
    protected String getBasePackage() {
        return "transforms/";
    }

    @Override
    protected String getDefaultTestPackage() {
        return "";
    }

    @Override
    protected void registerAll() {
        AddField addFieldTransformer = new AddField();
        registerNamed("field/NoField", "AddFieldToNoField", addFieldTransformer);
        registerNamed("field/OneField", "AddFieldToOneField", addFieldTransformer);
    }
}
