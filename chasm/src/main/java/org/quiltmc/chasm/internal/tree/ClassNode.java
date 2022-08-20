package org.quiltmc.chasm.internal.tree;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.objectweb.asm.ClassReader;
import org.quiltmc.chasm.api.util.ClassInfoProvider;
import org.quiltmc.chasm.internal.asm.visitor.ChasmClassVisitor;
import org.quiltmc.chasm.internal.metadata.PathMetadata;
import org.quiltmc.chasm.internal.util.NodeConstants;
import org.quiltmc.chasm.internal.util.PathInitializer;
import org.quiltmc.chasm.lang.api.ast.IntegerNode;
import org.quiltmc.chasm.lang.api.ast.ListNode;
import org.quiltmc.chasm.lang.api.ast.MapNode;
import org.quiltmc.chasm.lang.api.ast.Node;
import org.quiltmc.chasm.lang.api.ast.StringNode;
import org.quiltmc.chasm.lang.api.eval.Evaluator;
import org.quiltmc.chasm.lang.api.eval.Resolver;

public class ClassNode extends MapNode {
    private final ClassReader reader;

    public ClassNode(ClassReader reader, ClassInfoProvider classInfoProvider, int index) {
        super(new LazyMap<>(getStaticEntries(reader),
                () -> getLazyEntries(reader, classInfoProvider, index)));
        this.reader = reader;

        PathMetadata root = new PathMetadata(null, index);
        PathInitializer.initialize(this, root);
    }

    // NOTE: Ensure parity with ChasmClassVisitor
    private static Map<String, Node> getStaticEntries(ClassReader reader) {
        Map<String, Node> entries = new LinkedHashMap<>();

        entries.put(NodeConstants.ACCESS, new IntegerNode(reader.getAccess()));
        entries.put(NodeConstants.NAME, new StringNode(reader.getClassName()));
        entries.put(NodeConstants.SUPER, new StringNode(reader.getSuperName()));

        ListNode interfacesNode = new ListNode(new ArrayList<>());
        for (String iface : reader.getInterfaces()) {
            interfacesNode.getEntries().add(new StringNode(iface));
        }
        entries.put(NodeConstants.INTERFACES, interfacesNode);

        return entries;
    }

    public Map<String, Node> getStaticEntries() {
        return ((LazyMap<String, Node>) getEntries()).getStaticEntries();
    }

    public Map<String, Node> getLazyEntries() {
        return ((LazyMap<String, Node>) getEntries()).getLazyEntries();
    }

    private static Map<String, Node> getLazyEntries(ClassReader reader, ClassInfoProvider classInfoProvider,
                                                    int index) {
        ChasmClassVisitor visitor = new ChasmClassVisitor(classInfoProvider);
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
