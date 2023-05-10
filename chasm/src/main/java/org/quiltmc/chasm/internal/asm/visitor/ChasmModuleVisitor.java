package org.quiltmc.chasm.internal.asm.visitor;

import org.objectweb.asm.ModuleVisitor;
import org.quiltmc.chasm.internal.util.NodeConstants;
import org.quiltmc.chasm.lang.api.ast.Ast;
import org.quiltmc.chasm.lang.api.ast.ListNode;
import org.quiltmc.chasm.lang.api.ast.MapNode;

public class ChasmModuleVisitor extends ModuleVisitor {
    private final MapNode moduleNode;

    private final ListNode packages = Ast.emptyList();
    private final ListNode requires = Ast.emptyList();
    private final ListNode exports = Ast.emptyList();
    private final ListNode opens = Ast.emptyList();
    private final ListNode uses = Ast.emptyList();
    private final ListNode provides = Ast.emptyList();

    public ChasmModuleVisitor(int api, MapNode moduleNode) {
        super(api);
        this.moduleNode = moduleNode;

        moduleNode.put(NodeConstants.PACKAGES, packages);
        moduleNode.put(NodeConstants.REQUIRES, requires);
        moduleNode.put(NodeConstants.EXPORTS, exports);
        moduleNode.put(NodeConstants.OPENS, opens);
        moduleNode.put(NodeConstants.USES, uses);
        moduleNode.put(NodeConstants.PROVIDERS, provides);
    }

    @Override
    public void visitMainClass(String mainClass) {
        moduleNode.put(NodeConstants.MAIN, Ast.literal(mainClass));
    }

    @Override
    public void visitPackage(String packaze) {
        packages.add(Ast.literal(packaze));
    }

    @Override
    public void visitRequire(String module, int access, String version) {
        MapNode requireNode = Ast.map()
                .put(NodeConstants.MODULE, module)
                .put(NodeConstants.ACCESS, access)
                .put(NodeConstants.VERSION, version)
                .build();
        requires.add(requireNode);
    }

    @Override
    public void visitExport(String packaze, int access, String... modules) {
        MapNode exportNode = Ast.map()
                .put(NodeConstants.PACKAGE, packaze)
                .put(NodeConstants.ACCESS, access)
                .build();
        if (modules != null) {
            exportNode.put(NodeConstants.MODULES, Ast.list((Object[]) modules));
        }
        exports.add(exportNode);
    }

    @Override
    public void visitOpen(String packaze, int access, String... modules) {
        MapNode openNode = Ast.map()
                .put(NodeConstants.PACKAGE, packaze)
                .put(NodeConstants.ACCESS, access)
                .build();
        if (modules != null) {
            openNode.put(NodeConstants.MODULES, Ast.list((Object[]) modules));
        }
        opens.add(openNode);
    }

    @Override
    public void visitUse(String service) {
        uses.add(Ast.literal(service));
    }

    @Override
    public void visitProvide(String service, String... providers) {
        MapNode provideNode = Ast.map()
                .put(NodeConstants.SERVICE, service)
                .put(NodeConstants.PROVIDERS, Ast.list((Object[]) providers))
                .build();
        provides.add(provideNode);
    }

    @Override
    public void visitEnd() {
        // Nothing to do here
    }
}
