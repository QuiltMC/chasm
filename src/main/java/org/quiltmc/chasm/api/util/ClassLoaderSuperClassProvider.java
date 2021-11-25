package org.quiltmc.chasm.api.util;

public class ClassLoaderSuperClassProvider implements SuperClassProvider {
    private final SuperClassProvider parent;
    private final ClassLoader classLoader;

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
