package org.quiltmc.chasm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that this class should be used for testing class parsing and writing.
 * A class annotated with this annotation will be converted into Chasm's internal representation and back.
 * The test fails if the resulting class is different to the original class.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CheckUnchanged {
}
