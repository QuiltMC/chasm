package other;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@ExampleAnnotation(value = "Hi", list = { "First", "Second" }, nested = @SimpleAnnotation("Inner"))
public abstract class ExampleClass {
    public static void publicStaticMethod() {
        System.out.println("Hello Chasm!");
        int five = 5;
        try {
            System.out.println("5 = " + 5);
            int three = 3;
            System.out.println(five + " + " + three + " = " + (five + three));
        } catch (Exception e) {
            e.printStackTrace();
            five = 0;
        }

        publicStaticMethod();

        switch (five) {
            case 5:
                System.out.println("Still 5");
                break;
            default:
                System.out.println("No longer 5");
        }
    }

    public static String testSwitch() {
        return switch ((int) Math.round(Math.random() * 100)) {
            case 10 -> {
                String nested = "Test";
                yield switch (nested) {
                        case "NotTest" -> "NotTest";
                        default -> throw new IllegalStateException("Unexpected value: " + "Test");
                    };
            }
            default -> "Not 10";
        };
    }

    public static int testGenerics() throws ExecutionException, InterruptedException {
        int output = CompletableFuture.supplyAsync(() -> 5)
                .thenApply(i -> Integer.toString(i))
                .thenAccept(System.out::println)
                .thenApply(v -> 7)
                .get();

        switch (output) {
            case 7:
                output = 10;
                // fall through
            default:
                output = 7;
        }

        return output;
    }

    public abstract void annotationTest(@SimpleAnnotation("first") String first,
                                        @SimpleAnnotation("second") String second);

    public static record ExampleRecord(Integer first, String second) {

    }
}
