package other;

@SuppressWarnings("ALL")
public class TestMergeInsns {
    public static void target() {
        if (System.getProperty("Dummy Condition") == null) {
            System.out.println("Test Target Method");
        }
    }
}
