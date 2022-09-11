package org.quiltmc.chasm.api.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

/**
 * The context needed for chasm to function:
 * <ul>
 * <li>information about classes (superclass, type, etc)</li>
 * <li>cacheable file system access</li>
 * </ul>
 *
 * <p><a
 * href="https://docs.oracle.com/javase/specs/jvms/se17/html/jvms-4.html#jvms-4.2.1">
 * Binary names in JVMS 17</a>.
 *
 * <p>All methods in this class must be thread-safe, as they may be called concurrently.
 */
public interface Context {
    /**
     * Returns the name of the super class of a given class. The names are <a href="ClassLoader.html#binary-name">binary
     * names</a> as returned by {@link Class#getName()}, but with slashes '/' instead of dots '.'. For interfaces, this
     * should return "java/lang/Object".
     *
     * @param className The binary name of a class to query.
     *
     * @return The binary name of the super class of className.
     */
    @Contract(pure = true)
    String getSuperClass(String className);

    /**
     * Returns whether the input class is an interface.
     *
     * @param className The binary name of a class.
     * @return Whether the input class is an interface.
     */
    @Contract(pure = true)
    boolean isInterface(String className);

    /**
     * Returns whether the assignment {@code left = right} would succeed, where
     * {@code left} is of type {@code leftClass} and {@code right} is of type {@code rightClass}.
     *
     * @param leftClass The binary name of the left class.
     * @param rightClass The binary name of the right class.
     * @return Whether the assignment {@code left = right} would succeed.
     */
    @Contract(pure = true)
    boolean isAssignable(String leftClass, String rightClass);

    /**
     * Returns the contents of the file at the given path, or {@code null} if the file does not exist or if chasm
     * processors shouldn't have access to this file.
     *
     * <p><strong>Important</strong>: like the other functions in this interface, implementations of this function must
     * be pure, which means you should be mindful of the possibility of the file contents changing between invocations
     * of this method. A possible solution is to only read the file once and store it in memory.
     *
     * <p>It is possible to retain the cacheability of chasm while supporting this method, by remembering all files this
     * method is called with and their hashes, and storing them for the next time chasm is run (checking against the
     * hashes of the files then).
     *
     * <p>The format of the {@code path} is up to the implementer, for example different symbols might signify
     * different types of files (whether to look in a jar or in the file system). However, the accepted format of the
     * path should not depend on the operating system (e.g. \ must not be accepted on Windows if it is not going to be
     * accepted on Linux); this is to preserve purity across operating systems.
     *
     * @param path The implementation-dependent path to the file
     * @return The bytes in the file, or {@code null} if the file doesn't exist or is inaccessible
     */
    @Contract(pure = true)
    byte @Nullable [] readFile(String path);
}
