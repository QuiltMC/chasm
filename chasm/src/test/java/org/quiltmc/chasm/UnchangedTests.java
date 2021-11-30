package org.quiltmc.chasm;

/**
 * Tests for checking that converting a class into Chasm's internal representation and back don't change it.
 */
public class UnchangedTests extends TestsBase {
    @Override
    protected String getDefaultTestPackage() {
        return "";
    }

    @Override
    protected String getBasePackage() {
        return "unchanged/";
    }

    @Override
    protected void registerAll() {
        // Empty classes
        register("empty/EmptyClass");
        register("empty/EmptyInterface");
        register("empty/EmptyEnum");
        register("empty/EmptyAnnotation");
        register("empty/EmptyRecord");
        register("empty/EmptyOuterClass");
        register("empty/EmptyOuterClass$EmptyStaticNestedClass");
        register("empty/EmptyOuterClass$EmptyInnerClass");
        register("empty/EmptySealedClass");
        register("empty/EmptySealedExtendsClass");

        register("ExampleClass");
        register("ExampleClass$ExampleAnnotation");
        register("ExampleClass$ExampleRecord");
        register("ExampleEnum");
    }
}
