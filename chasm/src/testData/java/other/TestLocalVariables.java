package other;

import java.util.Random;

public class TestLocalVariables {
    static Random random = new Random(0);

    static void staticMethod(int param1, double param2) {
        int local1 = random.nextInt();
        local1++;
        double local2 = random.nextDouble();
        System.out.println(param1 + param2 + local1 + local2);
    }

    void instanceMethod(int param1, double param2) {
        int local1 = random.nextInt();
        double local2 = random.nextDouble();
        System.out.println(param1 + param2 + local1 + local2);
        System.out.println(this);
    }

    static void mergeVariable() {
        int local;
        if (random.nextBoolean()) {
            local = random.nextInt();
        } else {
            local = random.nextInt() + 1;
        }
        System.out.println(local);
        local = random.nextInt(); // reassignment, will be separate local
        System.out.println(local);
    }
}
