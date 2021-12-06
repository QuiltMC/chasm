package org.quiltmc.chasm.api.util;

/**
 * Provides the super class of named classes using the provided {@link ClassLoader}.
 *
 * <p>A {@link SuperClassProvider} implemented using the provided {@code ClassLoader},
 * as well as a parent {@code SuperClassProvider} to query when the provided {@code ClassLoader} fails to load a class.
 */
public class ClassLoaderSuperClassProvider implements SuperClassProvider {
    private final SuperClassProvider parent;
    private final ClassLoader classLoader;

    /**
     * Constructs a {@link ClassLoaderSuperClassProvider} using a {@link SuperClassProvider} parent and a
     * {@link ClassLoader}.
     *
     * @param parent The {@code SuperClassProvider} to query for classes not found in the given {@code ClassLoader}.
     * @param classLoader The {@code ClassLoader} with which to try to load the queried class.
     */
    public ClassLoaderSuperClassProvider(SuperClassProvider parent, ClassLoader classLoader) {
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
}
