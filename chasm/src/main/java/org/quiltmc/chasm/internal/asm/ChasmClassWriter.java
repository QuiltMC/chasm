package org.quiltmc.chasm.internal.asm;

import org.objectweb.asm.ClassWriter;
import org.quiltmc.chasm.api.util.Context;

public class ChasmClassWriter extends ClassWriter {
    private static final String OBJECT = "java/lang/Object";

    private final Context context;

    public ChasmClassWriter(Context context) {
        super(COMPUTE_FRAMES);

        this.context = context;
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
                current2 = context.getSuperClass(current2);
            }
            current1 = context.getSuperClass(current1);
        }

        return OBJECT;
    }
}
