package cofty.v4.core.parser;

import cofty.Utils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class BodyParserTest {
    @Test
    void validRootBodyAllAllowedElements() {
        final var expr = """
                
                
                
                var lavender = 0xE6E6FA
                abstractDrawer.draw(lavender)
                println(lavender!rgb)
                """;
        final var context = Utils.parseContextOf(expr);
        final var parseResult = BodyParser.ROOT_BODY.parse(context);

        Assertions.assertTrue(parseResult.isSuccessful(), "the parse result must be successful");

        Assertions.assertDoesNotThrow(
                parseResult::valueOrThrow,
                "the resulted ast object must be non null"
        );

        final var astObject = parseResult.valueOrThrow();
        final var countOfResidents = 3;

        Assertions.assertEquals(
                countOfResidents,
                astObject.residents.size(),
                String.format("there must be %s body expressions", countOfResidents)
        );
    }

    @Test
    void invalidNoWords() {
        final var expr = "";
        final var context = Utils.parseContextOf(expr);
        final var parseResult = BodyParser.ROOT_BODY.parse(context);

        Assertions.assertTrue(parseResult.isCanceled(), "the parse result must be specified as canceled");

        Assertions.assertNull(
                parseResult.value(),
                "the resulted ast object must be null"
        );
    }

    @Test
    void invalidNoSeparatorBetweenExpressions() {
        final var expr = "variable1 variable2";
        final var context = Utils.parseContextOf(expr);
        final var parseResult = BodyParser.ROOT_BODY.parse(context);

        Assertions.assertTrue(parseResult.isFailed(), "the parse result must be specified as failed");

        Assertions.assertNull(
                parseResult.value(),
                "the resulted ast object must be null"
        );
    }
}