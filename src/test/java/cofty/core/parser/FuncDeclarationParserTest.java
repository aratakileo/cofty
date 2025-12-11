package cofty.core.parser;

import cofty.Utils;
import cofty.type.Representable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.text.MessageFormat;
import java.util.Objects;

class FuncDeclarationParserTest {
    private static final FuncDeclarationParser MODULE_FUNC_DECLARATION_PARSER = FuncDeclarationParser.create(
            BodyParser.BodyType.MODULE
    );

    @Test
    void validWithSpecifiedArgsAndReturnType() {
        final var funcName = "iWannaGoHomeIWannaCallMyMommy";
        final var argName = "isEmergency";
        final var funcTypes = "bool";

        final var expr = String.format(
                "%s {println('Calling your mom...')\nreturn true}",
                MessageFormat.format("fun {0}({1}: {2} = false,) -> {2}", funcName, argName, funcTypes)
        );

        final var context = Utils.parseContextOf(expr);
        final var parseResult = MODULE_FUNC_DECLARATION_PARSER.parse(context);

        Assertions.assertTrue(parseResult.isSuccessful(), "the parse result must be successful");

        Assertions.assertDoesNotThrow(
                parseResult::valueOrThrow,
                "the resulted ast object must be non null"
        );

        final var astObject = parseResult.valueOrThrow();

        Assertions.assertEquals(
                funcName,
                astObject.name.content,
                String.format("invalid function name (`%s`)", Representable.repr(expr, true))
        );

        Assertions.assertEquals(
                1,
                astObject.args.size(),
                String.format(
                        "the function must contains exactly one argument (`%s`)",
                        Representable.repr(expr, true)
                )
        );

        Assertions.assertEquals(
                argName,
                astObject.args.getFirst().name.content,
                String.format(
                        "invalid name of the function first argument (`%s`)",
                        Representable.repr(expr, true)
                )
        );

        Assertions.assertEquals(
                funcTypes,
                Objects.requireNonNull(astObject.args.getFirst().valueType).name.getFirst().content,
                String.format(
                        "invalid value type of the function first argument (`%s`)",
                        Representable.repr(expr, true)
                )
        );

        Assertions.assertEquals(
                funcTypes,
                Objects.requireNonNull(astObject.returnType).name.getFirst().content,
                String.format(
                        "invalid function return type (`%s`)",
                        Representable.repr(expr, true)
                )
        );

        Assertions.assertEquals(
                2,
                astObject.body.residents.size(),
                String.format(
                        "the function body must contains exactly two body residents (`%s`)",
                        Representable.repr(expr, true)
                )
        );

        Assertions.assertTrue(astObject.body.finishedWithReturnStatement);
    }

    @Test
    void validNoArgsNoReturnType() {
        final var funcName = "nothing";
        final var expr = String.format("fun %s() {}", funcName);

        final var context = Utils.parseContextOf(expr);
        final var parseResult = MODULE_FUNC_DECLARATION_PARSER.parse(context);

        Assertions.assertTrue(parseResult.isSuccessful(), "the parse result must be successful");

        Assertions.assertDoesNotThrow(
                parseResult::valueOrThrow,
                "the resulted ast object must be non null"
        );

        final var astObject = parseResult.valueOrThrow();

        Assertions.assertEquals(
                funcName,
                astObject.name.content,
                String.format("invalid function name (`%s`)", Representable.repr(expr, true))
        );

        Assertions.assertEquals(
                0,
                astObject.args.size(),
                String.format(
                        "the function must contains no argument (`%s`)",
                        Representable.repr(expr, true)
                )
        );

        Assertions.assertNull(
                astObject.returnType,
                String.format(
                        "invalid function return type (`%s`)",
                        Representable.repr(expr, true)
                )
        );

        Assertions.assertEquals(
                0,
                astObject.body.residents.size(),
                String.format(
                        "the function body must be empty (`%s`)",
                        Representable.repr(expr, true)
                )
        );

        Assertions.assertFalse(astObject.body.finishedWithReturnStatement);
    }

    @Test
    void validEverythingStartsWithNewLine() {
        final var funcName = "iWannaGoHomeIWannaCallMyMommy";
        final var argName = "isEmergency";
        final var funcTypes = "bool";

        final var expr = String.format(
                "%s\n{\nprintln('Calling your mom...')\nreturn true\n}",
                MessageFormat.format(
                        "fun\n{0}\n(\n{1}\n:\n{2}\n=\nfalse\n,\n)\n->\n{2}",
                        funcName,
                        argName,
                        funcTypes
                )
        );

        final var context = Utils.parseContextOf(expr);
        final var parseResult = MODULE_FUNC_DECLARATION_PARSER.parse(context);

        Assertions.assertTrue(parseResult.isSuccessful(), "the parse result must be successful");

        Assertions.assertDoesNotThrow(
                parseResult::valueOrThrow,
                "the resulted ast object must be non null"
        );

        final var astObject = parseResult.valueOrThrow();

        Assertions.assertEquals(
                funcName,
                astObject.name.content,
                String.format("invalid function name (`%s`)", Representable.repr(expr, true))
        );

        Assertions.assertEquals(
                1,
                astObject.args.size(),
                String.format(
                        "the function must contains exactly one argument (`%s`)",
                        Representable.repr(expr, true)
                )
        );

        Assertions.assertEquals(
                argName,
                astObject.args.getFirst().name.content,
                String.format(
                        "invalid name of the function first argument (`%s`)",
                        Representable.repr(expr, true)
                )
        );

        Assertions.assertEquals(
                funcTypes,
                Objects.requireNonNull(astObject.args.getFirst().valueType).name.getFirst().content,
                String.format(
                        "invalid value type of the function first argument (`%s`)",
                        Representable.repr(expr, true)
                )
        );

        Assertions.assertEquals(
                funcTypes,
                Objects.requireNonNull(astObject.returnType).name.getFirst().content,
                String.format(
                        "invalid function return type (`%s`)",
                        Representable.repr(expr, true)
                )
        );

        Assertions.assertEquals(
                2,
                astObject.body.residents.size(),
                String.format(
                        "the function body must contains exactly two body residents (`%s`)",
                        Representable.repr(expr, true)
                )
        );

        Assertions.assertTrue(astObject.body.finishedWithReturnStatement);
    }

    @Test
    void validReturnStatementAtNestedBody() {
        final var expr = "fun sixtyNine() -> int {{return 69}} ";
        final var context = Utils.parseContextOf(expr);
        final var parseResult = MODULE_FUNC_DECLARATION_PARSER.parse(context);

        Assertions.assertTrue(parseResult.isSuccessful(), "the parse result must be successful");

        Assertions.assertDoesNotThrow(
                parseResult::valueOrThrow,
                "the resulted ast object must be non null"
        );

        final var astObject = parseResult.valueOrThrow();

        Assertions.assertEquals(
                1,
                astObject.body.residents.size(),
                String.format(
                        "the function body must contains exactly one body residents (`%s`)",
                        Representable.repr(expr, true)
                )
        );

        Assertions.assertTrue(astObject.body.finishedWithReturnStatement);
    }

    @Test
    void invalidNoValueReturnedButReturnValueDeclared() {
        final var expr = "fun invalid() -> int {}";

        final var context = Utils.parseContextOf(expr);
        final var parseResult = MODULE_FUNC_DECLARATION_PARSER.parse(context);

        Assertions.assertTrue(parseResult.isFailed(), "the parse result must be specified as failed");

        Assertions.assertNull(
                parseResult.value(),
                "the resulted ast object must be null"
        );

        Assertions.assertEquals(
                1,
                context.messages.handler.errCount(),
                "there must be exactly one compilation error message"
        );
    }

    @Test
    void invalidReturnType() {
        final var expr = "fun invalid() -> 34 {}";

        final var context = Utils.parseContextOf(expr);
        final var parseResult = MODULE_FUNC_DECLARATION_PARSER.parse(context);

        Assertions.assertTrue(parseResult.isFailed(), "the parse result must be specified as failed");

        Assertions.assertNull(
                parseResult.value(),
                "the resulted ast object must be null"
        );

        Assertions.assertEquals(
                1,
                context.messages.handler.errCount(),
                "there must be exactly one compilation error message"
        );
    }

    @Test
    void invalidNotSeparatedWithDotArguments() {
        final var expr = "fun invalid(a = 0 b = 1) {}";

        final var context = Utils.parseContextOf(expr);
        final var parseResult = MODULE_FUNC_DECLARATION_PARSER.parse(context);

        Assertions.assertTrue(parseResult.isFailed(), "the parse result must be specified as failed");

        Assertions.assertNull(
                parseResult.value(),
                "the resulted ast object must be null"
        );

        Assertions.assertEquals(
                1,
                context.messages.handler.errCount(),
                "there must be exactly one compilation error message"
        );
    }

    @Test
    void invalidArgumentDeclarationAsVariable() {
        final var expr = "fun invalid(var a = 0) {}";

        final var context = Utils.parseContextOf(expr);
        final var parseResult = MODULE_FUNC_DECLARATION_PARSER.parse(context);

        Assertions.assertTrue(parseResult.isFailed(), "the parse result must be specified as failed");

        Assertions.assertNull(
                parseResult.value(),
                "the resulted ast object must be null"
        );

        Assertions.assertEquals(
                1,
                context.messages.handler.errCount(),
                "there must be exactly one compilation error message"
        );
    }

    @Test
    void invalidArgumentWithNoExplicitlyDeclaredValueTypeAndAssignedWithNonSimpleValue() {
        final var expr = "fun invalid(a = invalid()) {}";

        final var context = Utils.parseContextOf(expr);
        final var parseResult = MODULE_FUNC_DECLARATION_PARSER.parse(context);

        Assertions.assertTrue(parseResult.isFailed(), "the parse result must be specified as failed");

        Assertions.assertNull(
                parseResult.value(),
                "the resulted ast object must be null"
        );

        Assertions.assertEquals(
                1,
                context.messages.handler.errCount(),
                "there must be exactly one compilation error message"
        );
    }

    @Test
    void invalidNoArgsSectionDescribed() {
        final var expr = "fun invalid";

        final var context = Utils.parseContextOf(expr);
        final var parseResult = MODULE_FUNC_DECLARATION_PARSER.parse(context);

        Assertions.assertTrue(parseResult.isFailed(), "the parse result must be specified as failed");

        Assertions.assertNull(
                parseResult.value(),
                "the resulted ast object must be null"
        );

        Assertions.assertEquals(
                1,
                context.messages.handler.errCount(),
                "there must be exactly one compilation error message"
        );
    }

    @Test
    void invalidArgsDescriptionNotFinishedCorrectly() {
        final var expr = "fun invalid(";

        final var context = Utils.parseContextOf(expr);
        final var parseResult = MODULE_FUNC_DECLARATION_PARSER.parse(context);

        Assertions.assertTrue(parseResult.isFailed(), "the parse result must be specified as failed");

        Assertions.assertNull(
                parseResult.value(),
                "the resulted ast object must be null"
        );

        Assertions.assertEquals(
                1,
                context.messages.handler.errCount(),
                "there must be exactly one compilation error message"
        );
    }

    @Test
    void invalidNoBodyDescribed() {
        final var expr = "fun invalid()";

        final var context = Utils.parseContextOf(expr);
        final var parseResult = MODULE_FUNC_DECLARATION_PARSER.parse(context);

        Assertions.assertTrue(parseResult.isFailed(), "the parse result must be specified as failed");

        Assertions.assertNull(
                parseResult.value(),
                "the resulted ast object must be null"
        );

        Assertions.assertEquals(
                1,
                context.messages.handler.errCount(),
                "there must be exactly one compilation error message"
        );
    }

    @Test
    void invalidBodyNotFinishedCorrectly() {
        final var expr = "fun invalid() {";

        final var context = Utils.parseContextOf(expr);
        final var parseResult = MODULE_FUNC_DECLARATION_PARSER.parse(context);

        Assertions.assertTrue(parseResult.isFailed(), "the parse result must be specified as failed");

        Assertions.assertNull(
                parseResult.value(),
                "the resulted ast object must be null"
        );

        Assertions.assertEquals(
                1,
                context.messages.handler.errCount(),
                "there must be exactly one compilation error message"
        );
    }
}