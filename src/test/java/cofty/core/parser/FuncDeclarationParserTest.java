package cofty.core.parser;

import cofty.core.compiler.diagnostic.Errors;
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

        final var astObject = ParseResultAssert.parse(expr, MODULE_FUNC_DECLARATION_PARSER)
                .ok()
                .hasNoDiagnosticMessages()
                .value();

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
        final var astObject = ParseResultAssert.parse(expr, MODULE_FUNC_DECLARATION_PARSER)
                .ok()
                .hasNoDiagnosticMessages()
                .value();

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

        final var astObject = ParseResultAssert.parse(expr, MODULE_FUNC_DECLARATION_PARSER)
                .ok()
                .hasNoDiagnosticMessages()
                .value();

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
        final var astObject = ParseResultAssert.parse(expr, MODULE_FUNC_DECLARATION_PARSER)
                .ok()
                .hasNoDiagnosticMessages()
                .value();

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
        ParseResultAssert.parse("fun invalid() -> int {}", MODULE_FUNC_DECLARATION_PARSER)
                .failed()
                .hasErrors(Errors.EXPECTED_FUNC_RETURN_STATEMENT);
    }

    @Test
    void invalidReturnType() {
        ParseResultAssert.parse("fun invalid() -> 34 {}", MODULE_FUNC_DECLARATION_PARSER)
                .failed()
                .hasErrors(Errors.EXPECTED_FUNC_RETURN_TYPE);
    }

    @Test
    void invalidNotSeparatedWithDotArguments() {
        ParseResultAssert.parse("fun invalid(a = 0 b = 1) {}", MODULE_FUNC_DECLARATION_PARSER)
                .failed()
                .hasErrors(Errors.MISSING_SEPARATOR);
    }

    @Test
    void invalidArgumentDeclarationAsVariable() {
        ParseResultAssert.parse("fun invalid(var a = 0) {}", MODULE_FUNC_DECLARATION_PARSER)
                .failed()
                .hasErrors(Errors.NOT_ALLOWED);
    }

    @Test
    void invalidArgumentWithNoExplicitlyDeclaredValueTypeAndAssignedWithNonSimpleValue() {
        ParseResultAssert.parse("fun invalid(a = invalid()) {}", MODULE_FUNC_DECLARATION_PARSER)
                .failed()
                .hasErrors(Errors.MISSING_EXPLICIT_TYPE);
    }

    @Test
    void invalidNoArgsSectionDescribed() {
        ParseResultAssert.parse("fun invalid", MODULE_FUNC_DECLARATION_PARSER)
                .failed()
                .hasErrors(Errors.NO_OPENED_BRACKETS);
    }

    @Test
    void invalidArgsDescriptionNotFinishedCorrectly() {
        ParseResultAssert.parse("fun invalid(", MODULE_FUNC_DECLARATION_PARSER)
                .failed()
                .hasErrors(Errors.UNCLOSED_BRACKETS);
    }

    @Test
    void invalidNoBodyDescribed() {
        ParseResultAssert.parse("fun invalid()", MODULE_FUNC_DECLARATION_PARSER)
                .failed()
                .hasErrors(Errors.NO_OPENED_BRACKETS);
    }

    @Test
    void invalidBodyNotFinishedCorrectly() {
        ParseResultAssert.parse("fun invalid() {", MODULE_FUNC_DECLARATION_PARSER)
                .failed()
                .hasErrors(Errors.UNCLOSED_BRACKETS);
    }
}