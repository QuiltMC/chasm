package org.quiltmc.chasm;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.quiltmc.chasm.api.util.ClassInfo;
import org.quiltmc.chasm.api.util.Context;
import org.quiltmc.chasm.internal.intrinsic.ChasmIntrinsics;
import org.quiltmc.chasm.internal.util.NodeUtils;
import org.quiltmc.chasm.lang.api.ast.IntegerNode;
import org.quiltmc.chasm.lang.api.ast.ListNode;
import org.quiltmc.chasm.lang.api.ast.MapNode;
import org.quiltmc.chasm.lang.api.ast.Node;
import org.quiltmc.chasm.lang.api.ast.NullNode;
import org.quiltmc.chasm.lang.api.ast.StringNode;
import org.quiltmc.chasm.lang.api.exception.EvaluationException;

public class IntrinsicsTest {
    private Node evaluate(String chassembly) {
        Context context = new Context() {
            @Override
            public @Nullable ClassInfo getClassInfo(String className) {
                try {
                    return ClassInfo.fromClass(Class.forName(className, false, getClass().getClassLoader()));
                } catch (ClassNotFoundException e) {
                    return null;
                }
            }

            @Override
            public byte @Nullable [] readFile(String path) {
                return switch (path) {
                    case "hello.txt" -> "Hello World!".getBytes(StandardCharsets.UTF_8);
                    case "hello.chasm" -> "file_content(\"hello.txt\")".getBytes(StandardCharsets.UTF_8);
                    case "lib.chasm" -> "{inc: val -> val + 1, result: inc(2)}".getBytes(StandardCharsets.UTF_8);
                    case "lib_invalid.chasm" -> "{invalid: outer}".getBytes(StandardCharsets.UTF_8);
                    default -> null;
                };
            }
        };
        Node node = Node.parse(chassembly);
        return node.evaluate(ChasmIntrinsics.makeEvaluator(node, context));
    }

    @Test
    public void testFileBytes() {
        ListNode bytes = (ListNode) evaluate("file_bytes(\"hello.txt\")");
        int[] byteArray = bytes.getEntries().stream()
                .mapToInt(NodeUtils::asInt).toArray();
        Assertions.assertArrayEquals(new int[] { 72, 101, 108, 108, 111, 32, 87, 111, 114, 108, 100, 33 }, byteArray);
    }

    @Test
    public void testFileContent() {
        StringNode string = (StringNode) evaluate("file_content(\"hello.txt\")");
        Assertions.assertEquals("Hello World!", string.getValue());
    }

    @Test
    public void testInclude() {
        StringNode string = (StringNode) evaluate("include(\"hello.chasm\")");
        Assertions.assertEquals("Hello World!", string.getValue());
    }

    @Test
    public void testFileContentInvalid() {
        Node result = evaluate("file_content(\"doesn't exist\")");
        Assertions.assertInstanceOf(NullNode.class, result);
    }

    @Test
    public void testLib1() {
        MapNode result = (MapNode) evaluate("{lib: include(\"lib.chasm\"), result: lib.inc(41)}");
        Assertions.assertEquals(42, ((IntegerNode) Objects.requireNonNull(result.get("result"))).getValue());
    }

    @Test
    public void testLib2() {
        MapNode result = (MapNode) evaluate("{lib: include(\"lib.chasm\"), result: lib.result}");
        Assertions.assertEquals(3, ((IntegerNode) Objects.requireNonNull(result.get("result"))).getValue());
    }

    @Test
    public void testIncludeCannotResolveOuter() {
        Assertions.assertThrows(EvaluationException.class, () -> evaluate("{outer: 42, lib: include(\"lib_invalid.chasm\")}"));
    }
}
