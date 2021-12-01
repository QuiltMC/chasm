import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.TraceClassVisitor;
import org.quiltmc.chasm.api.ChasmProcessor;
import org.quiltmc.chasm.api.util.ClassLoaderSuperClassProvider;
import org.quiltmc.chasm.lang.ChasmLang;
import org.quiltmc.chasm.lang.ChasmLangTransformer;
import org.quiltmc.chasm.lang.ReductionContext;
import org.quiltmc.chasm.lang.ast.MapExpression;

public class BasicTest {
    @Test
    public void parseAndRun() {
        String test = """
                {
                    int: 5 + 3,
                    bool: true,
                    string: "abc",
                    ref: $.int,
                    lambda: arg -> arg + 2,
                    call: $.lambda(4),
                    ternary: false ? "true" : "false",
                    equals: 5 = 3,
                    fibonacci: val -> val = 1 ? 1 : val = 2 ? 1 : $.fibonacci(val - 1) + $.fibonacci(val - 2),
                    call_fib: $.fibonacci(46),
                    curry: first -> second -> first - second,
                    call_curry: $.curry(5)(3),
                    list: [1, "two", false, { name: "object" }, none],
                    list_index: $.list.[1],
                    map_member: $.list.[3].name,
                    map_index: $.list.[3].["name"]
                }
                """;

        MapExpression map = ChasmLang.parse(test);
        MapExpression reduced = map.reduce(new ReductionContext());
    }

    @Test
    public void testTransform() throws IOException {
        ChasmProcessor processor =
                new ChasmProcessor(new ClassLoaderSuperClassProvider(null, getClass().getClassLoader()));

        byte[] classBytes = getClass().getResourceAsStream("TestClass.class").readAllBytes();
        processor.addClass(classBytes);

        String transformerString = """
                {
                    id: "exampleTransformer",
                    target_name: "TestClass",
                    target_class: classes.<c -> c.name = $.target_name>.[0],
                    transformations: [
                        {
                            target: {
                                node: $.target_class.methods,
                                start: 0,
                                end: 0,
                            },
                            apply: args -> [
                                {
                                    access: 1,
                                    name: "returnThis",
                                    descriptor: "()LTestClass;",
                                    code: {
                                        instructions: [
                                            {
                                                opcode: 25,
                                                var: 0
                                            },
                                            {
                                                opcode: 176,
                                            },
                                        ],
                                    },
                                }
                            ]
                        },
                    ],
                }
                """;
        ChasmLangTransformer transformer = ChasmLangTransformer.parse(transformerString);
        processor.addTransformer(transformer);

        List<byte[]> classes = processor.process();

        ClassReader reader = new ClassReader(classes.get(0));
        StringWriter resultString = new StringWriter();
        TraceClassVisitor resultVisitor = new TraceClassVisitor(new PrintWriter(resultString));
        reader.accept(resultVisitor, 0);

        System.out.println(resultString);
    }
}
