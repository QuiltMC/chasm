package org.quiltmc.chasm.internal.asm;

import org.objectweb.asm.ClassWriter;
import org.quiltmc.chasm.api.util.ClassInfoProvider;

public class ChasmClassWriter extends ClassWriter {
    private static final String OBJECT = "java/lang/Object";

    private final ClassInfoProvider classInfoProvider;

    public ChasmClassWriter(ClassInfoProvider classInfoProvider) {
        super(COMPUTE_FRAMES);

        this.classInfoProvider = classInfoProvider;
    }

    @Override
    protected String getCommonSuperClass(String type1, String type2) {
        String current1 = type1;
        while (!current1.equals(OBJECT)) {
            String current2 = type2;
            while (!current2.equals(OBJECT)) {
                if (current1.equals(current2)) {
                    return current1;
                }
                current2 = classInfoProvider.getSuperClass(current2);
            }
            current1 = classInfoProvider.getSuperClass(current1);
        }

        return OBJECT;
    }
}
