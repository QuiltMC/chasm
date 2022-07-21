import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.quiltmc.chasm.lang.api.ast.Node;
import org.quiltmc.chasm.lang.api.ast.LiteralNode;
import org.quiltmc.chasm.lang.api.ast.MapNode;
import org.quiltmc.chasm.lang.api.eval.Evaluator;
import org.quiltmc.chasm.lang.internal.render.Renderer;
import org.quiltmc.chasm.lang.internal.render.RendererConfig;
import org.quiltmc.chasm.lang.internal.render.RendererConfigBuilder;

public class BasicTest {

    @Test
    public void recursionTest() {
        String test = """
                {
                    run: state -> state.count = 0 ? "Done" : run({ count: state.count - 1 })
                }.run({count: 10})
                """;

        Node node = Node.parse(test);
        Node reduced = node.evaluate(Evaluator.create(node));
    }

    @Test
    public void ternaryTest() {
        String test = """
                {
                    val: 5 > (3 < 1 ? 4 : 7) ? 1 : 5 - (3 - 2)
                }.val
                """;

        Node node = Node.parse(test);
        Node reduced = node.evaluate(Evaluator.create(node));

        Assertions.assertInstanceOf(LiteralNode.class, reduced);
        Assertions.assertEquals(4L, ((LiteralNode) reduced).getValue());
    }

    @Test
    public void reverseResolve() {
        String test = """
                {
                    val: 1,
                    inner: {
                        val: 0,
                        result: $val - val
                    }
                }.inner.result
                """;

        Node node = Node.parse(test);
        Node reduced = node.evaluate(Evaluator.create(node));

        Assertions.assertInstanceOf(LiteralNode.class, reduced);
        Assertions.assertEquals(1L, ((LiteralNode) reduced).getValue());
    }

    @Test
    public void captureGlobal() {
        String test = """
                {
                    val: 1,
                    decrement: val -> val - $val
                }.decrement(2)
                """;

        Node node = Node.parse(test);
        Node reduced = node.evaluate(Evaluator.create(node));

        Assertions.assertInstanceOf(LiteralNode.class, reduced);
        Assertions.assertEquals(1L, ((LiteralNode) reduced).getValue());
    }

    @Test
    public void captureGlobalAndLocal() {
        String test = """
                {
                    val: 1,
                    inner: {
                        val: 2,
                        calc: arg -> val - $val
                    }
                }.inner.calc({})
                """;

        Node node = Node.parse(test);
        Node reduced = node.evaluate(Evaluator.create(node));

        Assertions.assertInstanceOf(LiteralNode.class, reduced);
        Assertions.assertEquals(1L, ((LiteralNode) reduced).getValue());
    }

    @Test
    public void recursionTest2() {
        String test = """
                {
                    run: state -> state = 0 ? "Done" : run(state - 1)
                }.run(10)
                """;

        Node node = Node.parse(test);
        Node reduced = node.evaluate(Evaluator.create(node));

        Assertions.assertInstanceOf(LiteralNode.class, reduced);
        Assertions.assertEquals("Done", ((LiteralNode) reduced).getValue());
    }

    @Test
    public void indirectRecursion() {
        String test = """
                {
                    run1: state -> state = 0 ? "Done" : run2(state - 1),
                    run2: state -> state = 0 ? "Done" : run1(state)
                }.run1(10)
                """;

        Node node = Node.parse(test);
        Node reduced = node.evaluate(Evaluator.create(node));

        Assertions.assertInstanceOf(LiteralNode.class, reduced);
        Assertions.assertEquals("Done", ((LiteralNode) reduced).getValue());
    }

    @Test
    public void currying() {
        String test = """
                {
                    curry: first -> second -> first - second
                }.curry(5)(3)
                """;

        Node node = Node.parse(test);
        Node reduced = node.evaluate(Evaluator.create(node));

        Assertions.assertInstanceOf(LiteralNode.class, reduced);
        Assertions.assertEquals(2L, ((LiteralNode) reduced).getValue());
    }

    @Test
    public void parseAndRun() {
        String test = """
                {
                    int: 5 + 3,
                    bool: true,
                    string: "abc",
                    ref: int,
                    self: int,
                    lambda: arg -> arg + 2,
                    call: lambda(4),
                    ternary: false ? "true" : "false",
                    equals: 5 = 3,
                    fibonacci: val -> val = 1 ? 1 : val = 2 ? 1 : fibonacci(val - 1) + fibonacci(val - 2),
                    call_fib: fibonacci(10),
                    curry: first -> second -> first - second,
                    call_curry: curry(5)(3),
                    list: [1, "two", false, { name: "object" }, null],
                    list_index: list[1],
                    map_member: list[3].name,
                    map_index: list[3]["name"],
                    concat: [1, 2] + [3, 4],
                    list_concat: [1, 2] + list,
                    filter_source: [{name: "hi"}, {name: "how"}, {name: "are"}, {name: "you"}],
                    filter: filter_source[entry -> chars(entry.name)[0] = 'h'],
                    compareWeird: arg -> arg > 3 = arg < 5 * -arg + 1000,
                    test: arg -> arg.a && arg.b || arg.c && arg.d,
                    test_call: test({ a: true, b: false, c: true, d: true }),
                    binop: x -> (x & ~7) | 1,
                    binop_call: binop(125),
                    shift: x -> x << 1,
                    shift_call: shift(125),
                    shifting: x -> x << 1 ^ x >> 1,
                    xmas: "Hello " + "World" + "!"
                }
                """;

        Node node = Node.parse(test);
        Node reduced = node.evaluate(Evaluator.create(node));

        Assertions.assertInstanceOf(MapNode.class, reduced);
    }

    @Test
    public void testBrainfuck() {
        // Simple brainfuck implementation and program
        String test = """
                join({
                    data_size: 20,
                    init_list: args -> args.length = 0 ? [] :
                        [args.value] + init_list({value: args.value, length: args.length - 1}),
                    init: {
                        ptr: 0,
                        data: init_list({
                            value: 0,
                            length: data_size
                        }),
                        pc: 0,
                        program: chars("
                            ++++++++++[>+++++++>++++++++++>+++>+<<<<-]
                            >++.>+.+++++++..+++.>++.<<+++++++++++++++.
                            >.+++.------.--------.>+."),
                        out: []
                    },
                    set: args ->
                        args.start = args.length ? args.result : set({
                            start: args.start + 1,
                            result: args.result + [args.start = args.index ? args.value : args.list[args.start]],
                            list: args.list,
                            length: args.length,
                            index: args.index,
                            value: args.value
                        }),
                    jmp_forward: args ->
                        args.depth = 0 ? args.pc :
                        args.program[args.pc] = '[' ? jmp_forward({
                            depth: args.depth + 1,
                            program: args.program,
                            pc: args.pc + 1
                        }) :
                        args.program[args.pc] = ']' ? jmp_forward({
                            depth: args.depth - 1,
                            program: args.program,
                            pc: args.pc + 1
                        }) :
                        jmp_forward({
                            depth: args.depth,
                            program: args.program,
                            pc: args.pc + 1
                        }),
                    jmp_back: args ->
                        args.depth = 0 ? args.pc + 2 :
                        args.program[args.pc] = '[' ?  jmp_back({
                            depth: args.depth - 1,
                            program: args.program,
                            pc: args.pc - 1
                        }) :
                        args.program[args.pc] = ']' ? jmp_back({
                            depth: args.depth + 1,
                            program: args.program,
                            pc: args.pc - 1
                        }) :
                        jmp_back({
                            depth: args.depth,
                            program: args.program,
                            pc: args.pc - 1
                        }),
                    run: state ->
                        state.program[state.pc] = null ? state.out :
                        state.program[state.pc] = '>' ? run({
                            ptr: state.ptr + 1,
                            data: state.data,
                            pc: state.pc + 1,
                            program: state.program,
                            out: state.out
                        }) :
                        state.program[state.pc] = '<' ? run({
                            ptr: state.ptr - 1,
                            data: state.data,
                            pc: state.pc + 1,
                            program: state.program,
                            out: state.out
                        }) :
                        state.program[state.pc] = '.' ? run({
                            ptr: state.ptr,
                            data: state.data,
                            pc: state.pc + 1,
                            program: state.program,
                            out: state.out + [state.data[state.ptr]]
                        }) :
                        state.program[state.pc] = '+' ? run({
                            ptr: state.ptr,
                            data: set({
                                start: 0,
                                result: [],
                                list: state.data,
                                length: data_size,
                                index: state.ptr,
                                value: state.data[state.ptr] + 1
                            }),
                            pc: state.pc + 1,
                            program: state.program,
                            out: state.out
                        }) :
                        state.program[state.pc] = '-' ? run({
                            ptr: state.ptr,
                            data: set({
                                start: 0,
                                result: [],
                                list: state.data,
                                length: data_size,
                                index: state.ptr,
                                value: state.data[state.ptr] - 1
                            }),
                            pc: state.pc + 1,
                            program: state.program,
                            out: state.out
                        }) :
                        state.program[state.pc] = '[' ? run({
                            ptr: state.ptr,
                            data: state.data,
                            pc: state.data[state.ptr] = 0 ?
                                jmp_forward({
                                    depth: 1,
                                    pc: state.pc + 1,
                                    program: state.program
                                }) :
                                state.pc + 1,
                            program: state.program,
                            out: state.out
                        }) :
                        state.program[state.pc] = ']' ? run({
                            ptr: state.ptr,
                            data: state.data,
                            pc: state.data[state.ptr] = 0 ?
                                state.pc + 1 :
                                jmp_back({
                                    depth: 1,
                                    pc: state.pc - 1,
                                    program: state.program
                                }),
                            program: state.program,
                            out: state.out
                        }) :
                        run({
                            ptr: state.ptr,
                            data: state.data,
                            pc: state.pc + 1,
                            program: state.program,
                            out: state.out
                        }),
                    result: run(init)
                }.result)
                """;

        long start = System.nanoTime();
        Node node = Node.parse(test);
        Node reduced = node.evaluate(Evaluator.create(node));

        Assertions.assertInstanceOf(LiteralNode.class, reduced);
        Object value = ((LiteralNode) reduced).getValue();
        Assertions.assertInstanceOf(String.class, value);
        Assertions.assertEquals("Hello World!", value);
        long end = System.nanoTime();
        System.out.println("Total time: " + (end - start) / 1e9);
    }
}
