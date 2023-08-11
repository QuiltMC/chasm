package org.quiltmc.chasm.api.util;

import java.util.Arrays;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;

/**
 * Static information related to a specific class.
 */
public class ClassInfo {
    public static final String OBJECT = "java/lang/Object";

    private final String className;
    private final String superClass;
    private final String[] interfaces;
    private final boolean isInterface;

    /**
     * Construct a new {@link ClassInfo}.
     *
     * @param className The name of the class (see {@link #getClassName()}).
     * @param superClass The name of the super class (see {@link #getSuperClass()}).
     * @param interfaces  The names of the implemented interfaces (see {@link #getInterfaces()}).
     * @param isInterface  Whether this class is an interface (see {@link #isInterface}).
     */
    public ClassInfo(String className, String superClass, String[] interfaces, boolean isInterface) {
        this.className = className;
        this.superClass = superClass;
        this.interfaces = interfaces;
        this.isInterface = isInterface;
    }

    /**
     * Returns the internal name of the class (See {@link org.objectweb.asm.Type#getInternalName}).
     *
     * @return The internal name of the class.
     */
    public String getClassName() {
        return className;
    }

    /**
     * Returns the internal name of the super class (See {@link org.objectweb.asm.Type#getInternalName}).
     * For interfaces, this should return {@link #OBJECT}.
     * Only for the {@link Object} class ({@link #OBJECT}) will this return null.
     *
     * @return The internal name of the super class.
     */
    public String getSuperClass() {
        return superClass;
    }

    /**
     * Returns the internal names of the implemented interfaces (See {@link org.objectweb.asm.Type#getInternalName}).
     *
     * @return The internal names of the interfaces.
     */
    public String[] getInterfaces() {
        return interfaces;
    }

    /**
     * Returns whether the class is an interface.
     *
     * @return Whether the class is an interface.
     */
    public boolean isInterface() {
        return isInterface;
    }

    /**
     * Extract class information from the provided {@link Class} object using reflection.
     *
     * @param clazz The class from which to extract information.
     * @return The information extracted from the provided class.
     */
    public static ClassInfo fromClass(Class<?> clazz) {
        if (clazz == Object.class) {
            return new ClassInfo(
                    ClassInfo.OBJECT,
                    null,
                    new String[0],
                    false
            );
        }

        return new ClassInfo(
                clazz.getName().replace('.', '/'),
                clazz.isInterface() ? ClassInfo.OBJECT : clazz.getSuperclass().getName().replace('.', '/'),
                Arrays.stream(clazz.getInterfaces()).map(c -> c.getName().replace('.', '/')).toArray(String[]::new),
                clazz.isInterface()
        );
    }

    /**
     * Extract class information from the provided binary representation of a class.
     * Such a binary representation can be obtained by reading a class file.
     *
     * @param classBytes The binary representation of a class, e.g. a class file.
     * @return The information extracted from the provided class.
     */
    public static ClassInfo fromBytes(byte[] classBytes) {
        ClassReader reader = new ClassReader(classBytes);
        return new ClassInfo(
                reader.getClassName(),
                reader.getSuperName(),
                reader.getInterfaces(),
                (reader.getAccess() & Opcodes.ACC_INTERFACE) != 0
        );
    }
}
