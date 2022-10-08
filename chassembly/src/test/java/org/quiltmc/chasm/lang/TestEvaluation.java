package org.quiltmc.chasm.lang;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Assertions;
import org.quiltmc.chasm.lang.api.ast.Node;
import org.quiltmc.chasm.lang.api.eval.Evaluator;
import org.quiltmc.chasm.lang.internal.render.Renderer;

public class TestEvaluation extends TestBase {
    @Override
    protected void doTest(Path testPath, Path resultPath) throws IOException {
        Node parsed = Node.parse(testPath);
        Node result = parsed.evaluate(Evaluator.create(parsed));
        StringBuilder builder = new StringBuilder();
        result.render(Renderer.builder().prettyPrinting(true).build(), builder, 1);
        String rendered = builder.toString();

        // If result doesn't exist yet, create file but fail anyway
        if (!Files.exists(resultPath)) {
            Files.createDirectories(resultPath.getParent());
            Files.write(resultPath, rendered.getBytes());
            Assertions.fail("Automatically created result file. Please verify results before committing.");
        }

        String expected = Files.readString(resultPath).replace("\r\n", "\n");
        Assertions.assertEquals(expected, rendered);
    }
}

