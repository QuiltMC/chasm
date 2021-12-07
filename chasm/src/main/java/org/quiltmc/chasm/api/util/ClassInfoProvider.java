package org.quiltmc.chasm.api.util;

/**
 * Provides the superclass of named classes, as JVMS binary names.
 *
 * <p><a
 * href="https://docs.oracle.com/javase/specs/jvms/se17/html/jvms-4.html#jvms-4.2.1">
 * Binary names in JVMS 17</a>
 */
public interface ClassInfoProvider {
    /**
     * Returns the name of the super class of a given class. The names are <a href="ClassLoader.html#binary-name">binary
     * names</a> as returned by {@link Class#getName()}, but with slashes '/' instead of dots '.'. For interfaces, this
     * should return "java/lang/Object".
     *
     * @param className The binary name of a class to query.
     *
     * @return The binary name of the super class of className.
     */
    String getSuperClass(String className);

    /**
     * Returns whether the input class is an interface.
     *
     * @param className The binary name of a class.
     * @return Whether the input class is an interface.
     */
    boolean isInterface(String className);

    /**
     * Returns whether the assignment {@code left = right} would succeed, where
     * {@code left} is of type {@code leftClass} and {@code right} is of type {@code rightClass}.
     *
     * @param leftClass The binary name of the left class.
     * @param rightClass The binary name of the right class.
     * @return Whether the assignment {@code left = right} would succeed.
     */
    boolean isAssignable(String leftClass, String rightClass);
}
