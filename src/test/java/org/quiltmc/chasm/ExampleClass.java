package org.quiltmc.chasm;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public abstract class ExampleClass {
    public static void publicStaticMethod() {
        System.out.println("Hello Chasm!");
        int five = 5;
        try {
            System.out.println("5 = " + 5);
            int three = 3;
            System.out.println(five + " + " + three + " = " + (five + three));
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        publicStaticMethod();
    }

    public abstract void annotationTest(@ExampleAnnotation("first") String first, @ExampleAnnotation("second") String second);

    @Retention(RetentionPolicy.RUNTIME)
    @interface ExampleAnnotation {
        String value();
    }

    public static record ExampleRecord(Integer first, String second) {

    }
}
