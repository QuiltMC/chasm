package other;

public class TestMergeInsns {
    public static void source() {
        for (int i = 0; i < 5; i++) {
            System.out.println("Test Source Method x"+i);
        }
    }

    public static void target() {
        if (System.getProperty("Dummy Condition") == null) {
            System.out.println("Test Target Method");
        }
    }
}
