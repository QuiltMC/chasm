package org.quiltmc.chasm;

/**
 * Tests for checking that converting a class into Chasm's internal representation and back don't change it.
 */
public class UnchangedTests extends TestsBase {
    @Override
    protected void registerAll() {
        // Empty classes
        register("empty/EmptyClass", "unchanged/EmptyClass");
        register("empty/EmptyInterface", "unchanged/EmptyInterface");
        register("empty/EmptyEnum", "unchanged/EmptyEnum");
        register("empty/EmptyAnnotation", "unchanged/EmptyAnnotation");
        register("empty/EmptyRecord", "unchanged/EmptyRecord");
        register("empty/EmptyOuterClass", "unchanged/EmptyOuterClass");
        register("empty/EmptyOuterClass$EmptyStaticNestedClass", "unchanged/EmptyOuterClass$EmptyStaticNestedClass");
        register("empty/EmptyOuterClass$EmptyInnerClass", "unchanged/EmptyOuterClass$EmptyInnerClass");
        register("empty/EmptySealedClass", "unchanged/EmptySealedClass");
        register("empty/EmptySealedExtendsClass", "unchanged/EmptySealedExtendsClass");

        register("other/ExampleClass", "unchanged/ExampleClass");
        register("other/ExampleClass$ExampleAnnotation", "unchanged/ExampleClass$ExampleAnnotation");
        register("other/ExampleClass$ExampleRecord", "unchanged/ExampleClass$ExampleRecord");
        register("other/ExampleEnum", "unchanged/ExampleEnum");
    }
}
