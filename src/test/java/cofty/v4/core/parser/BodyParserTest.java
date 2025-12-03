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
                
                {
                    println('Inside the nested body')
                }
                
                fun sum(a: int, b = 5) -> int {
                    return 7
                }
                """;
        final var context = Utils.parseContextOf(expr);
        final var parseResult = BodyParser.ROOT_BODY.parse(context);

        Assertions.assertTrue(parseResult.isSuccessful(), "the parse result must be successful");

        Assertions.assertDoesNotThrow(
                parseResult::valueOrThrow,
                "the resulted ast object must be non null"
        );

        final var astObject = parseResult.valueOrThrow();
        final var countOfResidents = 5;

        Assertions.assertEquals(
                countOfResidents,
                astObject.residents.size(),
                String.format("there must be exactly %s body expressions", countOfResidents)
        );
    }

    @Test
    void validSimpleNestedBody() {
        final var expr = "{}";
        final var context = Utils.parseContextOf(expr);
        final var parseResult = BodyParser.NESTED_BODY.parse(context);

        Assertions.assertTrue(parseResult.isSuccessful(), "the parse result must be successful");

        Assertions.assertDoesNotThrow(
                parseResult::valueOrThrow,
                "the resulted ast object must be non null"
        );

        final var astObject = parseResult.valueOrThrow();
        final var countOfResidents = 0;

        Assertions.assertEquals(
                countOfResidents,
                astObject.residents.size(),
                String.format("there must be exactly %s body expressions", countOfResidents)
        );
    }

    @Test
    void validSimpleNestedBodyWithNestedFuncCallStartsWithNewLine() {
        final var expr = """
                {
                    println('Hello World!')
                }
                """;
        final var context = Utils.parseContextOf(expr);
        final var parseResult = BodyParser.NESTED_BODY.parse(context);

        Assertions.assertTrue(parseResult.isSuccessful(), "the parse result must be successful");

        Assertions.assertDoesNotThrow(
                parseResult::valueOrThrow,
                "the resulted ast object must be non null"
        );

        final var astObject = parseResult.valueOrThrow();
        final var countOfResidents = 1;

        Assertions.assertEquals(
                countOfResidents,
                astObject.residents.size(),
                String.format("there must be exactly %s body expressions", countOfResidents)
        );
    }

    @Test
    void validNestedBodyInSimpleNestedBody() {
        final var expr = "{{}}";
        final var context = Utils.parseContextOf(expr);
        final var parseResult = BodyParser.NESTED_BODY.parse(context);

        Assertions.assertTrue(parseResult.isSuccessful(), "the parse result must be successful");

        Assertions.assertDoesNotThrow(
                parseResult::valueOrThrow,
                "the resulted ast object must be non null"
        );

        final var astObject = parseResult.valueOrThrow();
        final var countOfResidents = 1;

        Assertions.assertEquals(
                countOfResidents,
                astObject.residents.size(),
                String.format("there must be %s body expressions", countOfResidents)
        );
    }

    @Test
    void invalidUnclosedSimpleNestedBody() {
        final var expr = "{";
        final var context = Utils.parseContextOf(expr);
        final var parseResult = BodyParser.NESTED_BODY.parse(context);

        Assertions.assertTrue(parseResult.isFailed(), "the parse result must be specified as failed");

        Assertions.assertEquals(
                1,
                context.messages.handler.errCount(),
                "there must be exactly one compilation error message"
        );

        Assertions.assertNull(
                parseResult.value(),
                "the resulted ast object must be null"
        );
    }

    @Test
    void invalidUnclosedSimpleNestedBodyWithOtherParseFails() {
        final var expr = "{var test =";
        final var context = Utils.parseContextOf(expr);
        final var parseResult = BodyParser.NESTED_BODY.parse(context);

        Assertions.assertTrue(parseResult.isFailed(), "the parse result must be specified as failed");

        Assertions.assertEquals(
                2,
                context.messages.handler.errCount(),
                "there must be exactly two compilation error message"
        );

        Assertions.assertNull(
                parseResult.value(),
                "the resulted ast object must be null"
        );
    }

    @Test
    void invalidEmptyRootBody() {
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
    void invalidRootBodyNoSeparatorBetweenExpressions() {
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