package org.quiltmc.chasm;

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
        register("field/NoField", "transformer/field/AddField");
        register("field/OneField", "transformer/field/AddField");
    }
}
