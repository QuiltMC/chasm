package org.quiltmc.chasm.lang.internal.intrinsics;

import org.quiltmc.chasm.lang.api.ast.Ast;
import org.quiltmc.chasm.lang.api.ast.FloatNode;
import org.quiltmc.chasm.lang.api.ast.Node;
import org.quiltmc.chasm.lang.api.eval.Evaluator;
import org.quiltmc.chasm.lang.api.eval.IntrinsicFunction;
import org.quiltmc.chasm.lang.api.eval.SourceSpan;
import org.quiltmc.chasm.lang.api.exception.EvaluationException;

/**
 * Converts a float into its sign (integer), coefficient (float) and exponent (integer). Produces a map with these
 * keys and values. It is always the case that {@code sign * coefficient * (2.0 ^ exponent)} will produce the original
 * float.
 */
public class SplitFloatFunction extends IntrinsicFunction {
    @Override
    public Node apply(Evaluator evaluator, Node arg) {
        if (!(arg instanceof FloatNode)) {
            throw new EvaluationException(
                    getName() + " expected float, got " + arg,
                    arg.getMetadata().get(SourceSpan.class)
            );
        }
        double d = ((FloatNode) arg).getValue();
        long bits = Double.doubleToRawLongBits(d);

        int sign = (bits & Long.MIN_VALUE) == 0 ? 1 : -1;
        int exponent;
        double coefficient;
        if (d == 0.0) { // covers both positive and negative zero
            exponent = 0;
            coefficient = 0.0;
        } else if (Double.isNaN(d)) {
            sign = 1;
            exponent = 0;
            coefficient = Double.NaN;
        } else if (Double.isInfinite(d)) {
            exponent = 0;
            coefficient = Double.POSITIVE_INFINITY;
        } else {
            exponent = (int) (((bits >> 52) & ((1L << 11) - 1)) - 1023);
            long mantissa = bits & ((1L << 52) - 1);
            if (exponent == -1023) {
                // subnormal
                int factorBelowNormal = Long.numberOfLeadingZeros(mantissa) - 11;
                exponent -= factorBelowNormal - 1;
                mantissa <<= factorBelowNormal;
            }
            coefficient = Double.longBitsToDouble((1023L << 52) | mantissa);
        }

        return Ast.map()
                .put("sign", sign)
                .put("exponent", exponent)
                .put("coefficient", coefficient)
                .build();
    }

    @Override
    public String getName() {
        return "split_float";
    }
}
