package org.quiltmc.chasm.asm;

import org.objectweb.asm.FieldVisitor;
import org.quiltmc.chasm.tree.ChasmMap;

public class ChasmFieldVisitor extends FieldVisitor {
    private final ChasmMap fieldNode;

    public ChasmFieldVisitor(int api, ChasmMap fieldNode) {
        super(api);

        this.fieldNode = fieldNode;
    }

    // Todo: visitAnnotation

    // Todo: visitTypeAnnotation

    // Todo: visitAttribute

    // Todo: visitEnd
}
