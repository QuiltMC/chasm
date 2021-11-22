package unchanged;

public enum ExampleEnum {
    A(1), B(2) {
        public String toString() {
            return "b";
        }
    };

    private final int value;

    ExampleEnum(int value) {
        this.value = value;
    }
}
