package org.quiltmc.chasm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.quiltmc.chasm.transformer.Transformer;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CheckTransformed {
    Class<?> result();

    Class<? extends Transformer>[] transformer();

    Class<?>[] classes() default { };
}
