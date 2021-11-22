package org.quiltmc.chasm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.quiltmc.chasm.api.Transformer;

/**
 * Indicates that this class should be used for testing class transformations.
 * A class annotated with this annotation will be transformed by Chasm
 * given the specified transformers and additional classes.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CheckTransformed {
    String expected();

    Class<? extends Transformer>[] transformer();

    Class<?>[] classes() default { };

}
