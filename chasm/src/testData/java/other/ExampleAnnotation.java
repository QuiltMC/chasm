package other;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ExampleAnnotation {
    String value();

    String[] list();

    SimpleAnnotation nested();
}
