package org.quiltmc.chasm.lang.internal.intrinsics;

import org.quiltmc.chasm.lang.api.ast.FloatNode;
import org.quiltmc.chasm.lang.api.ast.IntegerNode;
import org.quiltmc.chasm.lang.api.ast.Node;
import org.quiltmc.chasm.lang.api.eval.Evaluator;
import org.quiltmc.chasm.lang.api.eval.IntrinsicFunction;
import org.quiltmc.chasm.lang.api.exception.EvaluationException;

public class ConvertSinglePrecisionFloatBits extends IntrinsicFunction {
    @Override
    public Node apply(Evaluator evaluator, Node arg) {
        if (arg instanceof IntegerNode) {
            return new FloatNode(Float.intBitsToFloat(((IntegerNode) arg).getValue().intValue()));
        } else if (arg instanceof FloatNode) {
            return new IntegerNode(Float.floatToRawIntBits(((FloatNode) arg).getValue().floatValue()));
        } else {
            throw new EvaluationException(getName() + " takes a float or an integer, got " + arg);
        }
    }

    @Override
    public String getName() {
        return "convert_single_precision_float_bits";
    }
}
