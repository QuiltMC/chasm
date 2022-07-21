package org.quiltmc.chasm.internal.transformer.tree;

import java.util.Iterator;

import org.antlr.v4.runtime.tree.ParseTree;
import org.quiltmc.chasm.api.tree.ListNode;
import org.quiltmc.chasm.api.tree.Node;
import org.quiltmc.chasm.lang.ast.AbstractListExpression;
import org.quiltmc.chasm.lang.ast.NullExpression;
import org.quiltmc.chasm.lang.op.Expression;

public class ListNodeNode extends AbstractListExpression implements NodeNode {
    private final ListNode listNode;

    public ListNodeNode(ParseTree tree, ListNode listNode) {
        super(tree);
        this.listNode = listNode;
    }

    @Override
    public Node getNode() {
        return listNode;
    }

    @Override
    public Expression get(ParseTree tree, int index) {
        if (0 <= index && index < listNode.size()) {
            return NodeNode.from(tree, listNode.get(index));
        } else {
            return new NullExpression(tree);
        }
    }

    @Override
    public Iterator<Expression> iterator() {
        return listNode.stream().map(n -> NodeNode.from(getParseTree(), n)).iterator();
    }

    @Override
    public int getLength() {
        return listNode.size();
    }
}
