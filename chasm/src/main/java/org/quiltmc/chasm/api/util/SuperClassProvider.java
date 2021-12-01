package org.quiltmc.chasm.api.util;

/**
 * Provides the superclass of a named class, as JVMS binary names.
 * <p>
 * <a href="https://docs.oracle.com/javase/specs/jvms/se17/html/jvms-4.html#jvms-4.2.1">
 * Binary names in JVMS 17
 * </a>
 */
public interface SuperClassProvider {
    /**
     * Returns the name of the super class of a given class.
     * The names are <a href="ClassLoader.html#binary-name">binary names</a> as returned by {@link Class#getName()},
     * but with slashes '/' instead of dots '.'.
     * For interfaces, this should return "java/lang/Object".
     *
     * @param className The binary name of a class.
     * @return The binary name of the super class of className.
     */
    String getSuperClass(String className);
}
