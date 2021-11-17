package org.quiltmc.chasm;

public class ExampleClass {
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
}
