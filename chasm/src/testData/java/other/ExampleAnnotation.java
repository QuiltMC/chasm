package other;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ExampleAnnotation {
    String value() default "Example";

    String[] list() default { "First", "Second"};

    SimpleAnnotation nested() default @SimpleAnnotation("Nested");
}
