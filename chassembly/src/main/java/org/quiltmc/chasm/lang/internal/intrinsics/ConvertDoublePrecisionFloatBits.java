package org.quiltmc.chasm.lang.internal.intrinsics;

import org.quiltmc.chasm.lang.api.ast.FloatNode;
import org.quiltmc.chasm.lang.api.ast.IntegerNode;
import org.quiltmc.chasm.lang.api.ast.Node;
import org.quiltmc.chasm.lang.api.eval.Evaluator;
import org.quiltmc.chasm.lang.api.eval.IntrinsicFunction;
import org.quiltmc.chasm.lang.api.exception.EvaluationException;

public class ConvertDoublePrecisionFloatBits extends IntrinsicFunction {
    @Override
    public Node apply(Evaluator evaluator, Node arg) {
        if (arg instanceof IntegerNode) {
            return new FloatNode(Double.longBitsToDouble(((IntegerNode) arg).getValue()));
        } else if (arg instanceof FloatNode) {
            return new IntegerNode(Double.doubleToRawLongBits(((FloatNode) arg).getValue()));
        } else {
            throw new EvaluationException(getName() + " takes a float or an integer, got " + arg);
        }
    }

    @Override
    public String getName() {
        return "convert_double_precision_float_bits";
    }
}
