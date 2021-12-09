package org.quiltmc.chasm.lang;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.quiltmc.chasm.api.Transformation;
import org.quiltmc.chasm.api.Transformer;
import org.quiltmc.chasm.api.target.NodeTarget;
import org.quiltmc.chasm.api.target.SliceTarget;
import org.quiltmc.chasm.api.target.Target;
import org.quiltmc.chasm.api.tree.FrozenListNode;
import org.quiltmc.chasm.api.tree.FrozenNode;
import org.quiltmc.chasm.api.tree.ListNode;
import org.quiltmc.chasm.api.tree.Node;
import org.quiltmc.chasm.lang.antlr.ChasmLexer;
import org.quiltmc.chasm.lang.antlr.ChasmParser;
import org.quiltmc.chasm.lang.ast.Expression;
import org.quiltmc.chasm.lang.ast.IntegerExpression;
import org.quiltmc.chasm.lang.ast.ListExpression;
import org.quiltmc.chasm.lang.ast.MapExpression;
import org.quiltmc.chasm.lang.ast.StringExpression;
import org.quiltmc.chasm.lang.interop.ChasmListNodeExpression;
import org.quiltmc.chasm.lang.interop.ChasmNodeExpression;
import org.quiltmc.chasm.lang.interop.ConversionHelper;
import org.quiltmc.chasm.lang.op.Callable;
import org.quiltmc.chasm.lang.visitor.ChasmExpressionVisitor;

public class ChasmLangTransformer implements Transformer {
    private final MapExpression mapExpression;

    private ChasmLangTransformer(MapExpression mapExpression) {
        this.mapExpression = mapExpression;
    }

    public static ChasmLangTransformer parse(CharStream charStream) {
        var chasmLexer = new ChasmLexer(charStream);
        TokenStream tokenStream = new CommonTokenStream(chasmLexer);
        var chasmParser = new ChasmParser(tokenStream);

        var fileContext = chasmParser.file();
        var mapVisitor = new ChasmExpressionVisitor();
        var mapExpression = mapVisitor.visitMap(fileContext.map());
        mapExpression.resolve("$", mapExpression);
        return new ChasmLangTransformer(mapExpression);
    }

    public static ChasmLangTransformer parse(String string) {
        return parse(CharStreams.fromString(string));
    }

    public static ChasmLangTransformer parse(Path path) throws IOException {
        return parse(CharStreams.fromPath(path));
    }

    @Override
    public Collection<Transformation> apply(FrozenListNode classes) {
        this.mapExpression.resolve("classes", new ChasmListNodeExpression(classes));

        var reduced = (MapExpression) new ReductionContext().reduce(this.mapExpression);

        var rawTransformationsExpression = reduced.get("transformations");
        if (!(rawTransformationsExpression instanceof ListExpression)) {
            throw new RuntimeException("Transformer must provide a list \"transformations\".");
        }

        var transformationsExpression = (ListExpression) rawTransformationsExpression;
        List<Transformation> transformations = new ArrayList<>(transformationsExpression.getEntries().size());
        for (Expression rawTransformationExpression : transformationsExpression.getEntries()) {
            transformations.add(this.parseTransformation(rawTransformationExpression));
        }

        return transformations;
    }

    private Transformation parseTransformation(Expression rawTransformationExpression) {
        if (!(rawTransformationExpression instanceof MapExpression)) {
            throw new RuntimeException("Transformation must be a map.");
        }

        // Extract target
        var transformationExpression = (MapExpression) rawTransformationExpression;
        var targetExpression = transformationExpression.get("target");
        var target = this.parseTarget(targetExpression);

        // Extract sources
        Map<String, Target> sources = new HashMap<>();
        var rawSourcesExpression = transformationExpression.get("sources");
        if (rawSourcesExpression != Expression.none()) {
            if (!(rawSourcesExpression instanceof MapExpression)) {
                throw new RuntimeException("Sources must be a map.");
            }
            var sourcesExpression = (MapExpression) rawSourcesExpression;
            for (Map.Entry<String, Expression> entry : sourcesExpression.getEntries().entrySet()) {
                sources.put(entry.getKey(), this.parseTarget(entry.getValue()));
            }
        }

        // Extract apply function
        var rawApplyExpression = transformationExpression.get("apply");
        if (!(rawApplyExpression instanceof Callable)) {
            throw new RuntimeException("Apply must be callable.");
        }
        var applyExpression = (Callable) rawApplyExpression;

        // Create Transformation
        return new Transformation() {
            @Override
            public Transformer getParent() {
                return ChasmLangTransformer.this;
            }

            @Override
            public Target getTarget() {
                return target;
            }

            @Override
            public Map<String, Target> getSources() {
                return sources;
            }

            @Override
            public FrozenNode apply(FrozenNode resolvedTarget, Map<String, ? extends Node> resolvedSources) {
                // Construct arguments
                Map<String, Expression> argEntries = new HashMap<>();
                argEntries.put("target", ConversionHelper.convert(resolvedTarget));
                argEntries.put("sources", ConversionHelper.convert((Node) resolvedSources));
                var argExpression = new MapExpression(argEntries);

                // Invoke function and return replacement
                var result = applyExpression.call(argExpression);
                return ConversionHelper.convert(result).asImmutable();
            }
        };
    }

    private Target parseTarget(Expression rawTargetExpression) {
        if (rawTargetExpression instanceof ChasmNodeExpression) {
            var targetNode = (ChasmNodeExpression) rawTargetExpression;
            return new NodeTarget(targetNode.getNode());
        } else if (rawTargetExpression instanceof MapExpression) {
            var targetExpression = (MapExpression) rawTargetExpression;

            var rawTarget = targetExpression.get("node");
            var rawStart = targetExpression.get("start");
            var rawEnd = targetExpression.get("end");

            if (!(rawTarget instanceof ChasmNodeExpression
                    && ((ChasmNodeExpression) rawTarget).getNode() instanceof ListNode)) {
                throw new RuntimeException("Slice target must contain list node as node.");
            }
            if (!(rawStart instanceof IntegerExpression && rawEnd instanceof IntegerExpression)) {
                throw new RuntimeException("Slice target must contain start and end as integers.");
            }

            var target = (ChasmNodeExpression) rawTarget;
            var start = (IntegerExpression) rawStart;
            var end = (IntegerExpression) rawEnd;

            return new SliceTarget(Node.asList(target.getNode()), start.getValue(), end.getValue());
        }

        throw new RuntimeException("Target mst be node or map.");
    }

    @Override
    public String getId() {
        var expression = this.mapExpression.get("id");
        if (expression instanceof StringExpression) {
            return ((StringExpression) expression).getValue();
        }

        throw new RuntimeException("Transformer doesn't provide an id.");
    }
}
