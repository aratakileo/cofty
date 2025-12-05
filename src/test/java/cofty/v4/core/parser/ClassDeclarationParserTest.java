package cofty.v4.core.parser;

import cofty.Utils;
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

        final var context = Utils.parseContextOf(expr);
        final var parseResult = MODULE_CLASS_DECLARATION_PARSER.parse(context);

        Assertions.assertTrue(parseResult.isSuccessful(), "the parse result must be successful");

        Assertions.assertDoesNotThrow(
                parseResult::valueOrThrow,
                "the resulted ast object must be non null"
        );

        final var astObject = parseResult.valueOrThrow();

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

        final var context = Utils.parseContextOf(expr);
        final var parseResult = MODULE_CLASS_DECLARATION_PARSER.parse(context);

        Assertions.assertTrue(parseResult.isSuccessful(), "the parse result must be successful");

        Assertions.assertDoesNotThrow(
                parseResult::valueOrThrow,
                "the resulted ast object must be non null"
        );

        final var astObject = parseResult.valueOrThrow();

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

        final var context = Utils.parseContextOf(expr);
        final var parseResult = MODULE_CLASS_DECLARATION_PARSER.parse(context);

        Assertions.assertTrue(parseResult.isSuccessful(), "the parse result must be successful");

        Assertions.assertDoesNotThrow(
                parseResult::valueOrThrow,
                "the resulted ast object must be non null"
        );

        final var astObject = parseResult.valueOrThrow();

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
    void invalidNonDeclarationResidentsInClassBody() {
        final var expr = """
                class InvalidResidentsInClass {
                    someFunctionCall()
                    someValue = 'newValue'
                    return 'whaaaat'
                    {}
                }
                """;

        final var context = Utils.parseContextOf(expr);
        final var parseResult = MODULE_CLASS_DECLARATION_PARSER.parse(context);

        Assertions.assertTrue(parseResult.isFailed(), "the parse result must be specified as failed");

        Assertions.assertNull(
                parseResult.value(),
                "the resulted ast object must be null"
        );

        Assertions.assertEquals(
                4,
                context.messages.handler.errCount(),
                "there must be exactly four compilation error message"
        );
    }
}