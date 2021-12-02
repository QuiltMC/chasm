package org.quiltmc.chasm.internal.util;

import java.io.PrintStream;
import java.util.Map;

import org.quiltmc.chasm.api.tree.ListNode;
import org.quiltmc.chasm.api.tree.MapNode;
import org.quiltmc.chasm.api.tree.Node;
import org.quiltmc.chasm.api.tree.ValueNode;
import org.quiltmc.chasm.internal.LazyClassNode;

public class TreePrinter {
    private static final String INDENT_STRING = "  ";

    private final PrintStream printStream;
    private final Boolean expandClasses;

    public TreePrinter(PrintStream printStream) {
        this(printStream, false);
    }

    public TreePrinter(PrintStream printStream, boolean expandClasses) {
        this.printStream = printStream;
        this.expandClasses = expandClasses;
    }

    public void print(Node node) {
        print(node, 0);
    }

    private void print(Node node, int indent) {
        if (node instanceof ValueNode) {
            ValueNode valueNode = (ValueNode) node;
            if (valueNode.getValue() instanceof String) {
                printStream.print("\"" + valueNode.getValue() + "\"");
            } else {
                printStream.print(valueNode.getValue());
            }
        } else if (node instanceof ListNode) {
            printStream.println("[");
            for (Node entry : (ListNode) node) {
                printIndent(indent + 1);
                print(entry, indent + 1);
                printStream.println(",");
            }
            printIndent(indent);
            printStream.print("]");
        } else if (node instanceof MapNode) {
            if (node instanceof LazyClassNode && !expandClasses) {
                printStream.print("LazyClassNode<" + ((LazyClassNode) node).getClassReader().getClassName() + ">");
            } else {
                printStream.println("{");
                for (Map.Entry<String, Node> entry : ((MapNode) node).entrySet()) {
                    printIndent(indent + 1);
                    printStream.print("\"" + entry.getKey() + "\"" + ": ");
                    print(entry.getValue(), indent + 1);
                    printStream.println(",");
                }
                printIndent(indent);
                printStream.print("}");
            }
        } else {
            throw new RuntimeException("Unexpected node type.");
        }
    }

    private void printIndent(int indent) {
        for (int i = 0; i < indent; i++) {
            printStream.print(INDENT_STRING);
        }
    }
}
