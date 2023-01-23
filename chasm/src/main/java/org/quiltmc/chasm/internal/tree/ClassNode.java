package org.quiltmc.chasm.internal.tree;

import java.util.LinkedHashMap;
import java.util.Map;

import org.objectweb.asm.ClassReader;
import org.quiltmc.chasm.api.util.Context;
import org.quiltmc.chasm.internal.asm.visitor.ChasmClassVisitor;
import org.quiltmc.chasm.internal.metadata.PathMetadata;
import org.quiltmc.chasm.internal.util.NodeConstants;
import org.quiltmc.chasm.internal.util.PathInitializer;
import org.quiltmc.chasm.lang.api.ast.Ast;
import org.quiltmc.chasm.lang.api.ast.MapNode;
import org.quiltmc.chasm.lang.api.ast.Node;
import org.quiltmc.chasm.lang.api.eval.Evaluator;
import org.quiltmc.chasm.lang.api.eval.Resolver;

public class ClassNode extends MapNode {
    private final ClassReader reader;

    public ClassNode(ClassReader reader, Context context, int index) {
        super(new LazyMap<>(getStaticEntries(reader),
                () -> getLazyEntries(reader, context, index)));
        this.reader = reader;

        PathMetadata root = new PathMetadata(null, index);
        PathInitializer.initialize(this, root);
    }

    // NOTE: Ensure parity with ChasmClassVisitor
    private static Map<String, Node> getStaticEntries(ClassReader reader) {
        Map<String, Node> entries = new LinkedHashMap<>();

        entries.put(NodeConstants.ACCESS, Ast.literal(reader.getAccess()));
        entries.put(NodeConstants.NAME, Ast.literal(reader.getClassName()));
        entries.put(NodeConstants.SUPER, Ast.nullableString(reader.getSuperName()));
        entries.put(NodeConstants.INTERFACES, Ast.list((Object[]) reader.getInterfaces()));

        return entries;
    }

    public Map<String, Node> getStaticEntries() {
        return ((LazyMap<String, Node>) getEntries()).getStaticEntries();
    }

    public Map<String, Node> getLazyEntries() {
        return ((LazyMap<String, Node>) getEntries()).getLazyEntries();
    }

    private static Map<String, Node> getLazyEntries(ClassReader reader, Context context,
                                                    int index) {
        ChasmClassVisitor visitor = new ChasmClassVisitor(context);
        reader.accept(visitor, 0);
        Map<String, Node> entries = visitor.getClassNode().getEntries();

        PathMetadata root = new PathMetadata(null, index);
        for (Map.Entry<String, Node> entry : entries.entrySet()) {
            PathMetadata path = new PathMetadata(root, entry.getKey());
            PathInitializer.initialize(entry.getValue(), path);
        }

        return entries;
    }

    public ClassReader getClassReader() {
        return reader;
    }

    @Override
    public void resolve(Resolver resolver) {
        // Class node can't contain references
    }

    @Override
    public Node evaluate(Evaluator evaluator) {
        // Class node can't be further simplified
        return this;
    }
}
