package cofty.core.parser;

import cofty.core.compiler.diagnostic.Errors;
import cofty.core.parser.parser.BodyParser;
import cofty.core.parser.parser.ClassDeclarationParser;
import cofty.type.Representable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ClassDeclarationParserTest {
    private static final ClassDeclarationParser MODULE_CLASS_DECLARATION_PARSER = ClassDeclarationParser.create(
            BodyParser.BodyType.MODULE
    );

    @Test
    void validEmptyClass() {
        final var className = "SixtyNine";
        final var expr = String.format("class %s {}", className);
        final var astObject = ParseResultAssert.parse(expr, MODULE_CLASS_DECLARATION_PARSER)
                .ok()
                .hasNoDiagnosticMessages()
                .value();

        Assertions.assertEquals(
                className,
                astObject.name.content,
                String.format("invalid class name (`%s`)", Representable.repr(expr, true))
        );

        Assertions.assertEquals(
                0,
                astObject.body.residents.size(),
                String.format(
                        "the class body must not contains any body residents (`%s`)",
                        Representable.repr(expr, true)
                )
        );
    }

    @Test
    void validEverythingStartsWithNewLine() {
        final var className = "SixtyNine";
        final var expr = String.format("class\n%s\n{\n}", className);
        final var astObject = ParseResultAssert.parse(expr, MODULE_CLASS_DECLARATION_PARSER)
                .ok()
                .hasNoDiagnosticMessages()
                .value();

        Assertions.assertEquals(
                className,
                astObject.name.content,
                String.format("invalid class name (`%s`)", Representable.repr(expr, true))
        );

        Assertions.assertEquals(
                0,
                astObject.body.residents.size(),
                String.format(
                        "the class body must not contains any body residents (`%s`)",
                        Representable.repr(expr, true)
                )
        );
    }

    @Test
    void validDeclarationResidentsInClassBody() {
        final var expr = """
                class SixtyNine {
                    class SixtyNine69 {}
                    var sixtyNine = 69
                    fun sixtyNine() -> int {
                        return 69
                    }
                }
                """;

        final var astObject = ParseResultAssert.parse(expr, MODULE_CLASS_DECLARATION_PARSER)
                .ok()
                .hasNoDiagnosticMessages()
                .value();

        Assertions.assertEquals(
                3,
                astObject.body.residents.size(),
                String.format(
                        "the class body must contains exactly three body resident (`%s`)",
                        Representable.repr(expr, true)
                )
        );
    }

    @Test
    void invalidFieldWithNoExplicitlyDeclaredValueTypeAndAssignedWithNonSimpleValue() {
        ParseResultAssert.parse("class SixtyNine {var ermmm = invalid()}", MODULE_CLASS_DECLARATION_PARSER)
                .failed()
                .hasErrors(Errors.MISSING_EXPLICIT_TYPE);
    }

    @Test
    void invalidNonDeclarationResidentsInClassBody() {
        ParseResultAssert.parse(
                        """
                        class InvalidResidentsInClass {
                            someFunctionCall()
                            someValue = 'newValue'
                            return 'whaaaat'
                            {}
                        }
                        """,
                        MODULE_CLASS_DECLARATION_PARSER
                ).failed().hasErrors(Errors.NOT_ALLOWED, Errors.NOT_ALLOWED, Errors.NOT_ALLOWED, Errors.NOT_ALLOWED);
    }
}