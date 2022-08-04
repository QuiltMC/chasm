package org.quiltmc.chasm.lang;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.quiltmc.chasm.lang.api.ast.BinaryNode;
import org.quiltmc.chasm.lang.api.ast.CallNode;
import org.quiltmc.chasm.lang.api.ast.IndexNode;
import org.quiltmc.chasm.lang.api.ast.IntegerNode;
import org.quiltmc.chasm.lang.api.ast.LambdaNode;
import org.quiltmc.chasm.lang.api.ast.ListNode;
import org.quiltmc.chasm.lang.api.ast.MapNode;
import org.quiltmc.chasm.lang.api.ast.MemberNode;
import org.quiltmc.chasm.lang.api.ast.Node;
import org.quiltmc.chasm.lang.api.ast.ReferenceNode;
import org.quiltmc.chasm.lang.api.ast.TernaryNode;
import org.quiltmc.chasm.lang.api.ast.UnaryNode;
import org.quiltmc.chasm.lang.api.ast.ValueNode;
import org.quiltmc.chasm.lang.internal.render.Renderer;

public class TestRendering extends TestBase {
    @Override
    protected void doTest(Path testPath, Path resultPath) throws IOException {
        Renderer renderer = Renderer.builder().prettyPrinting(true).trailingCommas(false).build();

        Node parsed = Node.parse(testPath);
        StringBuilder expectedBuilder = new StringBuilder();
        parsed.render(renderer, expectedBuilder, 1);
        String expected = expectedBuilder.toString();

        System.out.println(expected);

        Node parsedAgain = Node.parse(expected);

        assertEqual(parsed, parsedAgain);
    }

    private static void assertEqual(Node expected, Node actual) {
        if (expected.equals(actual)) {
            return;
        }

        if (expected instanceof MapNode) {
            Assertions.assertTrue(actual instanceof MapNode);

            Map<String, Node> expectedMap = ((MapNode) expected).getEntries();
            Map<String, Node> actualMap = ((MapNode) actual).getEntries();

            Assertions.assertEquals(expectedMap.size(), actualMap.size());

            for (Map.Entry<String, Node> entry : expectedMap.entrySet()) {
                assertEqual(entry.getValue(), actualMap.get(entry.getKey()));
            }
        } else if (expected instanceof BinaryNode) {
            Assertions.assertTrue(actual instanceof BinaryNode);

            assertEqual(((BinaryNode) expected).getLeft(), ((BinaryNode) actual).getLeft());
            assertEqual(((BinaryNode) expected).getRight(), ((BinaryNode) actual).getRight());
            Assertions.assertEquals(((BinaryNode) expected).getOperator(), ((BinaryNode) actual).getOperator());

        } else if (expected instanceof ValueNode<?>) {
            Assertions.assertTrue(actual instanceof ValueNode<?>);
            Assertions.assertEquals(((ValueNode<?>) expected).getValue(), ((ValueNode<?>) actual).getValue());
        } else if (expected instanceof ReferenceNode) {
            Assertions.assertTrue(actual instanceof ReferenceNode);
            Assertions.assertEquals(
                    ((ReferenceNode) expected).getIdentifier(), ((ReferenceNode) actual).getIdentifier());
            Assertions.assertEquals(((ReferenceNode) expected).isGlobal(), ((ReferenceNode) actual).isGlobal());
        } else if (expected instanceof LambdaNode) {
            Assertions.assertTrue(actual instanceof LambdaNode);
            Assertions.assertEquals(((LambdaNode) expected).getIdentifier(), ((LambdaNode) actual).getIdentifier());
            assertEqual(((LambdaNode) expected).getInner(), ((LambdaNode) actual).getInner());
        } else if (expected instanceof CallNode) {
            Assertions.assertTrue(actual instanceof CallNode);
            assertEqual(((CallNode) expected).getArg(), ((CallNode) actual).getArg());
            assertEqual(((CallNode) expected).getFunction(), ((CallNode) actual).getFunction());
        } else if (expected instanceof TernaryNode) {
            Assertions.assertTrue(actual instanceof TernaryNode);
            assertEqual(((TernaryNode) expected).getCondition(), ((TernaryNode) actual).getCondition());
            assertEqual(((TernaryNode) expected).getTrue(), ((TernaryNode) actual).getTrue());
            assertEqual(((TernaryNode) expected).getFalse(), ((TernaryNode) actual).getFalse());
        } else if (expected instanceof ListNode) {
            Assertions.assertTrue(actual instanceof ListNode);

            List<Node> expectedList = ((ListNode) expected).getEntries();
            List<Node> actualList = ((ListNode) actual).getEntries();

            Assertions.assertEquals(expectedList.size(), actualList.size());

            for (int i = 0; i < expectedList.size(); i++) {
                assertEqual(expectedList.get(i), actualList.get(i));
            }
        } else if (expected instanceof IndexNode) {
            Assertions.assertTrue(actual instanceof IndexNode);
            assertEqual(((IndexNode) expected).getLeft(), ((IndexNode) actual).getLeft());
            assertEqual(((IndexNode) expected).getIndex(), ((IndexNode) actual).getIndex());
        } else if (expected instanceof MemberNode) {
            Assertions.assertTrue(actual instanceof MemberNode);
            assertEqual(((MemberNode) expected).getLeft(), ((MemberNode) actual).getLeft());
            Assertions.assertEquals(((MemberNode) expected).getIdentifier(), ((MemberNode) actual).getIdentifier());
        } else if (expected instanceof UnaryNode) {
            Assertions.assertTrue(actual instanceof UnaryNode);
            assertEqual(((UnaryNode) expected).getInner(), ((UnaryNode) actual).getInner());
            Assertions.assertEquals(((UnaryNode) expected).getOperator(), ((UnaryNode) actual).getOperator());
        } else {
            Assertions.fail("unknown type: " + expected.getClass().getSimpleName());
        }
    }
}
