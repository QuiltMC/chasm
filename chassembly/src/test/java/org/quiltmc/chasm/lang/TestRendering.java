package org.quiltmc.chasm.lang;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.Assertions;
import org.quiltmc.chasm.lang.api.ast.Node;
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
        StringBuilder actualBuilder = new StringBuilder();
        parsedAgain.render(renderer, actualBuilder, 1);
        String actual = actualBuilder.toString();

        Assertions.assertEquals(expected, actual);
    }
}
