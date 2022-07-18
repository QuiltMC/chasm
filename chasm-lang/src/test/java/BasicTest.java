import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.quiltmc.chasm.lang.api.ast.Expression;
import org.quiltmc.chasm.lang.api.ast.LiteralExpression;
import org.quiltmc.chasm.lang.api.ast.MapExpression;
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

        Expression expression = Expression.parse(test);
        Expression reduced = Evaluator.create().evaluate(expression);

        Assertions.assertInstanceOf(LiteralExpression.class, reduced);
        Assertions.assertEquals("Done", ((LiteralExpression) reduced).getValue());
        RendererConfig config = RendererConfigBuilder.create(4, ' ').prettyPrinting().insertEndingNewline().build();
        String firstRender = Renderer.render(expression, config);
        Expression firstRenderParsed = Expression.parse(firstRender);
        String secondRender = Renderer.render(firstRenderParsed, config);
        Assertions.assertEquals(firstRender, secondRender);
        System.out.println(firstRender);
        if (test.equals(secondRender)) {
            System.out.println("input syntax and double parsed syntax are identical");
        }
    }

    @Test
    public void ternaryTest() {
        String test = """
                {
                    val: 5 > (3 < 1 ? 4 : 7) ? 1 : 5 - (3 - 2)
                }.val
                """;

        Expression expression = Expression.parse(test);
        Expression reduced = Evaluator.create().evaluate(expression);

//        Assertions.assertInstanceOf(LiteralExpression.class, reduced);
//        Assertions.assertEquals(4L, ((LiteralExpression) reduced).getValue());
        RendererConfig config = RendererConfigBuilder.create(4, ' ').prettyPrinting().insertEndingNewline().build();
        String firstRender = Renderer.render(expression, config);
        Expression firstRenderParsed = Expression.parse(firstRender);
        String secondRender = Renderer.render(firstRenderParsed, config);
        Assertions.assertEquals(firstRender, secondRender);
        System.out.println(firstRender);
        if (test.equals(secondRender)) {
            System.out.println("input syntax and double parsed syntax are identical");
        }
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

        Expression expression = Expression.parse(test);
        Expression reduced = Evaluator.create().evaluate(expression);

        Assertions.assertInstanceOf(LiteralExpression.class, reduced);
        Assertions.assertEquals(1L, ((LiteralExpression) reduced).getValue());
        RendererConfig config = RendererConfigBuilder.create(4, ' ').prettyPrinting().insertEndingNewline().build();
        String firstRender = Renderer.render(expression, config);
        Expression firstRenderParsed = Expression.parse(firstRender);
        String secondRender = Renderer.render(firstRenderParsed, config);
        Assertions.assertEquals(firstRender, secondRender);
        System.out.println(firstRender);
        if (test.equals(secondRender)) {
            System.out.println("input syntax and double parsed syntax are identical");
        }
    }

    @Test
    public void recursionTest2() {
        String test = """
                {
                    run: state -> state = 0 ? "Done" : run(state - 1)
                }.run(10)
                """;

        Expression expression = Expression.parse(test);
        Expression reduced = Evaluator.create().evaluate(expression);

        Assertions.assertInstanceOf(LiteralExpression.class, reduced);
        Assertions.assertEquals("Done", ((LiteralExpression) reduced).getValue());
        RendererConfig config = RendererConfigBuilder.create(4, ' ').prettyPrinting().insertEndingNewline().build();
        String firstRender = Renderer.render(expression, config);
        Expression firstRenderParsed = Expression.parse(firstRender);
        String secondRender = Renderer.render(firstRenderParsed, config);
        Assertions.assertEquals(firstRender, secondRender);
        System.out.println(firstRender);
        if (test.equals(secondRender)) {
            System.out.println("input syntax and double parsed syntax are identical");
        }
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

        Expression expression = Expression.parse(test);
        Expression reduced = Evaluator.create().evaluate(expression);

        Assertions.assertInstanceOf(MapExpression.class, reduced);

        RendererConfig config = RendererConfigBuilder.create(4, ' ').prettyPrinting().insertEndingNewline().build();
        String firstRender = Renderer.render(expression, config);
        Expression firstRenderParsed = Expression.parse(firstRender);
        String secondRender = Renderer.render(firstRenderParsed, config);
        Assertions.assertEquals(firstRender, secondRender);
        System.out.println(firstRender);
        if (test.equals(secondRender)) {
            System.out.println("input syntax and double parsed syntax are identical");
        }
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
        Expression expression = Expression.parse(test);
        Expression reduced = Evaluator.create().evaluate(expression);

        Assertions.assertInstanceOf(LiteralExpression.class, reduced);
        Object value = ((LiteralExpression) reduced).getValue();
        Assertions.assertInstanceOf(String.class, value);
        Assertions.assertEquals("Hello World!", value);
        long end = System.nanoTime();
        System.out.println("Total time: " + (end - start) / 1e9);

        RendererConfig config = RendererConfigBuilder.create(4, ' ').prettyPrinting().insertEndingNewline().build();
        String firstRender = Renderer.render(expression, config);
        Expression firstRenderParsed = Expression.parse(firstRender);
        String secondRender = Renderer.render(firstRenderParsed, config);
        Assertions.assertEquals(firstRender, secondRender);
        System.out.println(firstRender);
        if (test.equals(secondRender)) {
            System.out.println("input syntax and double parsed syntax are identical");
        }
    }
}
