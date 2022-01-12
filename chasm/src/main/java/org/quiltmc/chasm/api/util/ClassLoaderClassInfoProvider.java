package org.quiltmc.chasm.api.util;

/**
 * Provides the super class of named classes using the provided {@link ClassLoader}.
 *
 * <p>A {@link ClassInfoProvider} implemented using the provided {@code ClassLoader},
 * as well as a parent {@code SuperClassProvider} to query when the provided {@code ClassLoader} fails to load a class.
 */
public class ClassLoaderClassInfoProvider implements ClassInfoProvider {
    private final ClassInfoProvider parent;
    private final ClassLoader classLoader;

    /**
     * Constructs a {@link ClassLoaderClassInfoProvider} using a {@link ClassInfoProvider} parent and a
     * {@link ClassLoader}.
     *
     * @param parent The {@code ClassInfoProvider} to query for classes not found in the given {@code ClassLoader}.
     * @param classLoader The {@code ClassLoader} with which to try to load the queried class.
     */
    public ClassLoaderClassInfoProvider(ClassInfoProvider parent, ClassLoader classLoader) {
        this.parent = parent;
        this.classLoader = classLoader;
    }

    @Override
    public String getSuperClass(String className) {
        try {
            Class<?> clazz = Class.forName(className.replace('/', '.'), false, classLoader);
            if (clazz.isInterface()) {
                return "java/lang/Object";
            }

            return clazz.getSuperclass().getName().replace('.', '/');
        } catch (ClassNotFoundException e) {
            if (parent == null) {
                throw new RuntimeException(e);
            }
            return parent.getSuperClass(className);
        }
    }

    @Override
    public boolean isInterface(String className) {
        try {
            Class<?> clazz = Class.forName(className.replace('/', '.'), false, classLoader);
            return clazz.isInterface();
        } catch (ClassNotFoundException e) {
            if (parent == null) {
                throw new RuntimeException(e);
            }
            return parent.isInterface(className);
        }
    }

    @Override
    public boolean isAssignable(String leftClass, String rightClass) {
        try {
            Class<?> leftClazz = Class.forName(leftClass.replace('/', '.'), false, classLoader);
            Class<?> rightClazz = Class.forName(rightClass.replace('/', '.'), false, classLoader);
            return leftClazz.isAssignableFrom(rightClazz);
        } catch (ClassNotFoundException e) {
            if (parent == null) {
                throw new RuntimeException(e);
            }
            return parent.isAssignable(leftClass, rightClass);
        }
    }
}
