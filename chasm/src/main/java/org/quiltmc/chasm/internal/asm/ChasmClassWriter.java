package org.quiltmc.chasm.internal.asm;

import org.objectweb.asm.ClassWriter;
import org.quiltmc.chasm.api.util.SuperClassProvider;

public class ChasmClassWriter extends ClassWriter {
    private static final String OBJECT = "java/lang/Object";

    private final SuperClassProvider superClassProvider;

    public ChasmClassWriter(SuperClassProvider superClassProvider) {
        super(COMPUTE_FRAMES);

        this.superClassProvider = superClassProvider;
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
                current2 = superClassProvider.getSuperClass(current2);
            }
            current1 = superClassProvider.getSuperClass(current1);
        }

        return OBJECT;
    }
}
