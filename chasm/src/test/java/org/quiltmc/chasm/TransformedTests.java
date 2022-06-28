package org.quiltmc.chasm;

/**
 * Tests for checking transformations.
 */
public class TransformedTests extends TestsBase {
    @Override
    protected void registerAll() {
        register("empty/EmptyClass", "add/field_to_empty", "add_field");
        register("empty/EmptyClass", "add/method_to_empty", "add_method");

        register("other/TestLocalVariables", "other/test_local_variables", "test_local_variables");
    }
}
