package cofty.core.parser;

import cofty.ParseResultAssert;
import cofty.core.compiler.diagnostic.Errors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class BodyParserTest {
    private final static BodyParser MODULE_NESTED_BODY_PARSER = BodyParser.MODULE_BODY.getNestedBodyParser();

    @Test
    void validModuleBodyAllAllowedElements() {
        final var astObject = ParseResultAssert.parse(
                """
                
                
                
                var lavender = 0xE6E6FA
                abstractDrawer.draw(lavender)
                println(lavender!rgb)
                
                {
                    println('Inside the nested body')
                }
                
                fun sum(a: int, b = 5) -> int {
                    return 7
                }
                
                2 ** 2 * 2 ** 2 ** 2 / 4 + 1
                """,
                BodyParser.MODULE_BODY
        ).ok().hasNoDiagnosticMessages().value();

        final var countOfResidents = 6;

        Assertions.assertEquals(
                countOfResidents,
                astObject.residents.size(),
                String.format("there must be exactly %s body expressions", countOfResidents)
        );
    }

    @Test
    void validEmptyNestedBody() {
        final var astObject = ParseResultAssert.parse("{}", MODULE_NESTED_BODY_PARSER)
                .ok()
                .hasNoDiagnosticMessages()
                .value();

        final var countOfResidents = 0;

        Assertions.assertEquals(
                countOfResidents,
                astObject.residents.size(),
                String.format("there must be exactly %s body expressions", countOfResidents)
        );
    }

    @Test
    void validNestedBodyWithNestedFuncCallThatSpecifiedRightAfterNewLine() {
        final var astObject = ParseResultAssert.parse(
                        """
                        {
                            println('Hello World!')
                        }
                        """,
                        MODULE_NESTED_BODY_PARSER
                ).ok().hasNoDiagnosticMessages().value();

        final var countOfResidents = 1;

        Assertions.assertEquals(
                countOfResidents,
                astObject.residents.size(),
                String.format("there must be exactly %s body expressions", countOfResidents)
        );
    }

    @Test
    void validEmptyNestedBodyInNestedBody() {
        final var astObject = ParseResultAssert.parse("{{}}", MODULE_NESTED_BODY_PARSER)
                .ok()
                .hasNoDiagnosticMessages()
                .value();

        final var countOfResidents = 1;

        Assertions.assertEquals(
                countOfResidents,
                astObject.residents.size(),
                String.format("there must be %s body expressions", countOfResidents)
        );
    }

    @Test
    void invalidUnclosedEmptyNestedBody() {
        ParseResultAssert.parse("{", MODULE_NESTED_BODY_PARSER)
                .failed()
                .hasErrors(Errors.UNCLOSED_BRACKETS);
    }

    @Test
    void invalidUnclosedNestedBodyWithOtherParseFails() {
        ParseResultAssert.parse("{var test =", MODULE_NESTED_BODY_PARSER)
                .failed()
                .hasErrors(Errors.EXPECTED_ASSIGNABLE_VALUE, Errors.UNCLOSED_BRACKETS);
    }

    @Test
    void invalidModuleNestedBodyFunctionDeclaration() {
        ParseResultAssert.parse("{fun invalid() {}}", MODULE_NESTED_BODY_PARSER)
                .failed()
                .hasErrors(Errors.NOT_ALLOWED);
    }

    @Test
    void invalidEmptyModuleBody() {
        ParseResultAssert.parse("", BodyParser.MODULE_BODY)
                .skipped()
                .hasNoDiagnosticMessages();
    }

    @Test
    void invalidModuleBodyNoSeparatorBetweenExpressions() {
        ParseResultAssert.parse("variable1 variable2", BodyParser.MODULE_BODY)
                .failed()
                .hasErrors(Errors.MISSING_SEPARATOR);
    }

    @Test
    void invalidModuleBodyReturnStatement() {
        ParseResultAssert.parse("return", BodyParser.MODULE_BODY)
                .failed()
                .hasErrors(Errors.NOT_ALLOWED);
    }
}