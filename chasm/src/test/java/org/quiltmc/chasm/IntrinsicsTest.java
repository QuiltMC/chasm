package org.quiltmc.chasm;

import java.nio.charset.StandardCharsets;

import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.quiltmc.chasm.api.util.ClassLoaderContext;
import org.quiltmc.chasm.api.util.Context;
import org.quiltmc.chasm.internal.intrinsic.ChasmIntrinsics;
import org.quiltmc.chasm.lang.api.ast.IntegerNode;
import org.quiltmc.chasm.lang.api.ast.ListNode;
import org.quiltmc.chasm.lang.api.ast.MapNode;
import org.quiltmc.chasm.lang.api.ast.Node;
import org.quiltmc.chasm.lang.api.ast.NullNode;
import org.quiltmc.chasm.lang.api.ast.StringNode;

public class IntrinsicsTest {
    @Test
    public void testIntrinsics() {
        Context context = new ClassLoaderContext(null, getClass().getClassLoader()) {
            @Override
            public byte @Nullable [] readFile(String path) {
                return switch (path) {
                    case "hello.txt" -> "Hello World!".getBytes(StandardCharsets.UTF_8);
                    case "hello.chasm" -> "file_content(\"hello.txt\")".getBytes(StandardCharsets.UTF_8);
                    case "lib.chasm" -> "{inc: val -> val + 1, result: inc(2)}".getBytes(StandardCharsets.UTF_8);
                    default -> null;
                };
            }
        };

        Node node = Node.parse("""
                {
                    bytes: file_bytes("hello.txt"),
                    string: file_content("hello.txt"),
                    included: include("hello.chasm"),
                    invalid: file_content("doesn't exist"),
                    lib: include("lib.chasm"),
                    test_lib: lib.inc(41),
                    test_lib_2: lib.result
                }
                """);
        MapNode result = (MapNode) node.evaluate(ChasmIntrinsics.makeEvaluator(node, context));

        ListNode bytes = (ListNode) result.getEntries().get("bytes");
        int[] byteArray = bytes.getEntries().stream()
                .mapToInt(entry -> ((IntegerNode) entry).getValue().intValue()).toArray();
        Assertions.assertArrayEquals(new int[] { 72, 101, 108, 108, 111, 32, 87, 111, 114, 108, 100, 33 }, byteArray);

        StringNode string = (StringNode) result.getEntries().get("string");
        Assertions.assertEquals("Hello World!", string.getValue());

        StringNode included = (StringNode) result.getEntries().get("included");
        Assertions.assertEquals("Hello World!", included.getValue());

        Assertions.assertInstanceOf(NullNode.class, result.getEntries().get("invalid"));

        IntegerNode testLib = (IntegerNode) result.getEntries().get("test_lib");
        Assertions.assertEquals(42, testLib.getValue().intValue());

        IntegerNode testLib2 = (IntegerNode) result.getEntries().get("test_lib_2");
        Assertions.assertEquals(3, testLib2.getValue().intValue());
    }
}
