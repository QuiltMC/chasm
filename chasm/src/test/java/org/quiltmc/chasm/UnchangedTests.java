package org.quiltmc.chasm;

/**
 * Tests for checking that converting a class into Chasm's internal representation and back don't change it.
 */
public class UnchangedTests extends TestsBase {
    @Override
    protected void registerAll() {
        // Empty classes
        register("empty/EmptyClass", "unchanged/EmptyClass", "touch");
        register("empty/EmptyInterface", "unchanged/EmptyInterface", "touch");
        register("empty/EmptyEnum", "unchanged/EmptyEnum", "touch");
        register("empty/EmptyAnnotation", "unchanged/EmptyAnnotation", "touch");
        register("empty/EmptyRecord", "unchanged/EmptyRecord", "touch");
        register("empty/EmptyOuterClass", "unchanged/EmptyOuterClass", "touch");
        register("empty/EmptyOuterClass$EmptyStaticNestedClass", "unchanged/EmptyOuterClass$EmptyStaticNestedClass", "touch");
        register("empty/EmptyOuterClass$EmptyInnerClass", "unchanged/EmptyOuterClass$EmptyInnerClass", "touch");
        register("empty/EmptySealedClass", "unchanged/EmptySealedClass", "touch");
        register("empty/EmptySealedExtendsClass", "unchanged/EmptySealedExtendsClass", "touch");

        register("other/ExampleClass", "unchanged/ExampleClass", "touch");
        register("other/ExampleAnnotation", "unchanged/ExampleAnnotation", "touch");
        register("other/ExampleClass$ExampleRecord", "unchanged/ExampleClass$ExampleRecord", "touch");
        register("other/ExampleEnum", "unchanged/ExampleEnum", "touch");
    }
}
