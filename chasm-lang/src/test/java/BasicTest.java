import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.TraceClassVisitor;
import org.quiltmc.chasm.api.ChasmProcessor;
import org.quiltmc.chasm.api.ClassData;
import org.quiltmc.chasm.api.util.ClassLoaderClassInfoProvider;
import org.quiltmc.chasm.lang.ChasmLang;
import org.quiltmc.chasm.lang.ChasmLangTransformer;
import org.quiltmc.chasm.lang.ReductionContext;
import org.quiltmc.chasm.lang.ast.Expression;
import org.quiltmc.chasm.lang.ast.IntegerExpression;
import org.quiltmc.chasm.lang.ast.ListExpression;
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
                    map_index: $.list.[3].["name"],
                    concat: [1, 2] + [3, 4],
                    list_concat: [1, 2] + $.list,
                    compareWeird: arg -> arg > 3 = arg < 5 * -arg + 1000,
                    test: arg -> arg.a && arg.b || arg.c && arg.d,
                    test_call: $.test({ a: true, b: false, c: true, d: true }),
                    binop: x -> (x & ~7) | 1,
                    binop_call: $.binop(125),
                    shift: x -> x << 1,
                    shift_call: $.shift(125),
                    shifting: x -> x << 1 ^ x >> 1,
                    xmas: "Hello " + "World" + "!"
                }
                """;

        MapExpression map = ChasmLang.parse(test);
        MapExpression reduced = (MapExpression) new ReductionContext().reduce(map);
    }

    @Test
    public void testTransform() throws IOException {
        ChasmProcessor processor =
                new ChasmProcessor(new ClassLoaderClassInfoProvider(null, getClass().getClassLoader()));

        byte[] classBytes = getClass().getResourceAsStream("TestClass.class").readAllBytes();
        processor.addClass(new ClassData(classBytes));

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
                                    parameters: [],
                                    returnType: T"LTestClass;",
                                    code: {
                                        instructions: [
                                            {
                                                opcode: 25,
                                                var: "this"
                                            },
                                            {
                                                opcode: 176,
                                            },
                                        ]
                                    },
                                }
                            ]
                        },
                    ],
                }
                """;
        ChasmLangTransformer transformer = ChasmLangTransformer.parse(transformerString);
        processor.addTransformer(transformer);

        List<ClassData> classes = processor.process();

        ClassReader reader = new ClassReader(classes.get(0).getClassBytes());
        StringWriter resultString = new StringWriter();
        TraceClassVisitor resultVisitor = new TraceClassVisitor(new PrintWriter(resultString));
        reader.accept(resultVisitor, 0);

        System.out.println(resultString);
    }

    @Test
    public void testLocalIndexes() throws IOException {
        ChasmProcessor processor =
                new ChasmProcessor(new ClassLoaderClassInfoProvider(null, getClass().getClassLoader()));

        byte[] classBytes = getClass().getResourceAsStream("TestLocalVariables.class").readAllBytes();
        processor.addClass(new ClassData(classBytes));

        // TODO: remove len when there is a builtin length function
        String transformerString = """
                {
                    len_helper: map -> map.list.[map.index] = none ? map.index
                        : $.len_helper({list: map.list, index: map.index + 1}),
                    len: list -> $.len_helper({list: list, index: 0}),
                    tail: info -> {
                        node: $.target_class.methods.<m -> m.name = info.target_method>.[0].code.instructions,
                        start: $.len($.transformations.[info.index].target.node) * 2 - 3,
                        end: $.transformations.[info.index].target.start
                    },
                    id: "exampleTransformer",
                    target_name: "TestLocalVariables",
                    target_class: classes.<c -> c.name = $.target_name>.[0],
                    transformations: [
                        {
                            target: $.tail({target_method: "staticMethod", index: 0}),
                            sources: {
                                var_name: $.transformations.[0].target.node.<i -> i.opcode = 54>.[0].var
                            },
                            apply: args -> [
                                {
                                    opcode: 132,
                                    var: args.sources.var_name,
                                    increment: 1
                                },
                            ],
                        },
                        {
                            target: $.tail({target_method: "instanceMethod", index: 1}),
                            sources: {
                                var_name: $.transformations.[1].target.node.<i -> i.opcode = 54>.[0].var
                            },
                            apply: args -> [
                                {
                                    opcode: 132,
                                    var: args.sources.var_name,
                                    increment: 1
                                },
                            ],
                        },
                        {
                            target: $.tail({target_method: "mergeVariable", index: 2}),
                            sources: {
                                var1: $.transformations.[2].target.node.<i -> i.opcode = 54>.[0].var,
                                var2: $.transformations.[2].target.node
                                    .<i -> i.opcode = 54 ? i.var = $.transformations.[2].sources.var1
                                        ? false : true : false>
                                    .[0].var
                            },
                            apply: args -> [
                                {
                                    opcode: 132,
                                    var: args.sources.var1,
                                    increment: 1
                                },
                                {
                                    opcode: 132,
                                    var: args.sources.var2,
                                    increment: 1
                                },
                            ],
                        },
                    ],
                    
                }
                """;
        ChasmLangTransformer transformer = ChasmLangTransformer.parse(transformerString);
        processor.addTransformer(transformer);

        List<ClassData> classes = processor.process();

        ClassReader reader = new ClassReader(classes.get(0).getClassBytes());
        StringWriter resultString = new StringWriter();
        TraceClassVisitor resultVisitor = new TraceClassVisitor(new PrintWriter(resultString));
        reader.accept(resultVisitor, 0);

        System.out.println(resultString);
    }

    @Test
    public void testBrainfuck() {
        String test = """
                {
                    data_size: 20,
                    init_list: args -> args.length = 0 ? [] :
                        [args.value] + $.init_list({value: args.value, length: args.length - 1}),
                    init: {
                        ptr: 0,
                        data: $.init_list({
                            value: 0,
                            length: $.data_size
                        }),
                        pc: 0,
                        program: "
                            ++++++++++[>+++++++>++++++++++>+++>+<<<<-]
                            >++.>+.+++++++..+++.>++.<<+++++++++++++++.
                            >.+++.------.--------.>+.",
                        out: []
                    },
                    set: args ->
                        args.start = args.length ? args.result : $.set({
                            start: args.start + 1,
                            result: args.result + [args.start = args.index ? args.value : args.list.[args.start]],
                            list: args.list,
                            length: args.length,
                            index: args.index,
                            value: args.value
                        }),
                    jmp_forward: args ->
                        args.depth = 0 ? args.pc :
                        args.program.[args.pc] = "[" ? $.jmp_forward({
                            depth: args.depth + 1,
                            program: args.program,
                            pc: args.pc + 1
                        }) :
                        args.program.[args.pc] = "]" ? $.jmp_forward({
                            depth: args.depth - 1,
                            program: args.program,
                            pc: args.pc + 1
                        }) :
                        $.jmp_forward({
                            depth: args.depth,
                            program: args.program,
                            pc: args.pc + 1
                        }),
                    jmp_back: args ->
                        args.depth = 0 ? args.pc + 2 :
                        args.program.[args.pc] = "[" ?  $.jmp_back({
                            depth: args.depth - 1,
                            program: args.program,
                            pc: args.pc - 1
                        }) :
                        args.program.[args.pc] = "]" ? $.jmp_back({
                            depth: args.depth + 1,
                            program: args.program,
                            pc: args.pc - 1
                        }) :
                        $.jmp_back({
                            depth: args.depth,
                            program: args.program,
                            pc: args.pc - 1
                        }),
                    run: state ->
                        state.program.[state.pc] = none ? state.out :
                        state.program.[state.pc] = ">" ? $.run({
                            ptr: state.ptr + 1,
                            data: state.data,
                            pc: state.pc + 1,
                            program: state.program,
                            out: state.out
                        }) :
                        state.program.[state.pc] = "<" ? $.run({
                            ptr: state.ptr - 1,
                            data: state.data,
                            pc: state.pc + 1,
                            program: state.program,
                            out: state.out
                        }) :
                        state.program.[state.pc] = "." ? $.run({
                            ptr: state.ptr,
                            data: state.data,
                            pc: state.pc + 1,
                            program: state.program,
                            out: state.out + [state.data.[state.ptr]]
                        }) :
                        state.program.[state.pc] = "+" ? $.run({
                            ptr: state.ptr,
                            data: $.set({
                                start: 0,
                                result: [],
                                list: state.data,
                                length: $.data_size,
                                index: state.ptr,
                                value: state.data.[state.ptr] + 1
                            }),
                            pc: state.pc + 1,
                            program: state.program,
                            out: state.out
                        }) :
                        state.program.[state.pc] = "-" ? $.run({
                            ptr: state.ptr,
                            data: $.set({
                                start: 0,
                                result: [],
                                list: state.data,
                                length: $.data_size,
                                index: state.ptr,
                                value: state.data.[state.ptr] - 1
                            }),
                            pc: state.pc + 1,
                            program: state.program,
                            out: state.out
                        }) :
                        state.program.[state.pc] = "[" ? $.run({
                            ptr: state.ptr,
                            data: state.data,
                            pc: state.data.[state.ptr] = 0 ?
                                $.jmp_forward({
                                    depth: 1,
                                    pc: state.pc + 1,
                                    program: state.program
                                }) :
                                state.pc + 1,
                            program: state.program,
                            out: state.out
                        }) :
                        state.program.[state.pc] = "]" ? $.run({
                            ptr: state.ptr,
                            data: state.data,
                            pc: state.data.[state.ptr] = 0 ?
                                state.pc + 1 :
                                $.jmp_back({
                                    depth: 1,
                                    pc: state.pc - 1,
                                    program: state.program
                                }),
                            program: state.program,
                            out: state.out
                        }) :
                        $.run({
                            ptr: state.ptr,
                            data: state.data,
                            pc: state.pc + 1,
                            program: state.program,
                            out: state.out
                        }),
                    result: $.run($.init)
                }
                """;

        MapExpression map = ChasmLang.parse(test);
        MapExpression reduced = (MapExpression) new ReductionContext().reduce(map);
        List<Expression> expressions = ((ListExpression) reduced.get("result")).getEntries();
        char[] chars = new char[expressions.size()];
        for (int i = 0; i < expressions.size(); i++) {
            IntegerExpression integerExpression = (IntegerExpression) expressions.get(i);
            chars[i] = (char) integerExpression.getValue().intValue();
        }
        String result = String.valueOf(chars);
        Assertions.assertEquals("Hello World!", result);
    }
}
