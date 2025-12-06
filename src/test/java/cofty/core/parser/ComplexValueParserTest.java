package cofty.core.parser;

import cofty.Utils;
import cofty.core.lexer.token.type.Simple;
import cofty.core.parser.ComplexValueParser;
import cofty.type.Representable;
import cofty.core.parser.ast.value.complex.ComplexValueObject;
import cofty.core.parser.ast.value.complex.FieldAccessObject;
import cofty.core.parser.ast.value.complex.FuncCallObject;
import cofty.core.parser.ast.value.complex.SimpleValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ComplexValueParserTest {
    @Test
    void validSingleFieldAccess() {
        final var expr = "validField";
        final var context = Utils.parseContextOf(expr);
        final var parseResult = ComplexValueParser.DEFAULT.parse(context);

        Assertions.assertTrue(parseResult.isSuccessful(), "the parse result must be successful");

        Assertions.assertDoesNotThrow(
                parseResult::valueOrThrow,
                "the resulted ast object must be non null"
        );

        Assertions.assertInstanceOf(FieldAccessObject.class, parseResult.valueOrThrow());

        final var astObject = (FieldAccessObject)parseResult.valueOrThrow();

        Assertions.assertEquals(
                "validField",
                astObject.name.content,
                String.format("invalid field name (`%s`)", expr)
        );
    }

    @Test
    void validSingleIntValue() {
        final var expr = "51";
        final var context = Utils.parseContextOf(expr);
        final var parseResult = ComplexValueParser.DEFAULT.parse(context);

        Assertions.assertTrue(parseResult.isSuccessful(), "the parse result must be successful");

        Assertions.assertDoesNotThrow(
                parseResult::valueOrThrow,
                "the resulted ast object must be non null"
        );

        Assertions.assertInstanceOf(SimpleValue.class, parseResult.valueOrThrow());

        final var astObject = (SimpleValue)parseResult.valueOrThrow();

        Assertions.assertEquals(Simple.INT, astObject.value.type);

        Assertions.assertEquals(
                "51",
                astObject.value.content,
                String.format("invalid value, expected `%s`", expr)
        );
    }

    @Test
    void validSingleFuncCall() {
        final var name = "validFuncCall";
        final var expr = String.format("%s()", name);
        final var context = Utils.parseContextOf(expr);
        final var parseResult = ComplexValueParser.DEFAULT.parse(context);

        Assertions.assertTrue(parseResult.isSuccessful(), "the parse result must be successful");

        Assertions.assertDoesNotThrow(
                parseResult::valueOrThrow,
                "the resulted ast object must be non null"
        );

        Assertions.assertInstanceOf(FuncCallObject.class, parseResult.valueOrThrow());

        final var astObject = (FuncCallObject)parseResult.valueOrThrow();

        Assertions.assertEquals(
                name,
                astObject.name.content,
                String.format("invalid callable function name (`%s`)", expr)
        );

        Assertions.assertTrue(
                astObject.args.isEmpty(),
                String.format("the function call must contain no arguments (`%s`)", expr)
        );

        Assertions.assertFalse(
                astObject.isPostfix,
                String.format("the function call must not be postfix call (`%s`)", expr)
        );
    }

    @Test
    void validSingleFuncCallWithOneArg() {
        final var name = "validFuncCall";
        final var argValue = "'что-то мега крутое на русском (by avi)'";
        final var expr = String.format("%s(%s,)", name, argValue);
        final var context = Utils.parseContextOf(expr);
        final var parseResult = ComplexValueParser.DEFAULT.parse(context);

        Assertions.assertTrue(parseResult.isSuccessful(), "the parse result must be successful");

        Assertions.assertDoesNotThrow(
                parseResult::valueOrThrow,
                "the resulted ast object must be non null"
        );

        Assertions.assertInstanceOf(FuncCallObject.class, parseResult.valueOrThrow());

        final var astObject = (FuncCallObject)parseResult.valueOrThrow();

        Assertions.assertEquals(
                name,
                astObject.name.content,
                String.format("invalid callable function name (`%s`)", expr)
        );

        Assertions.assertEquals(
                1,
                astObject.args.size(),
                String.format("the function call must contain exactly one argument (`%s`)", expr)
        );

        Assertions.assertInstanceOf(
                SimpleValue.class,
                astObject.args.getFirst(),
                String.format("the argument of the function call must be simple str value (`%s`)", expr)
        );

        Assertions.assertEquals(
                argValue,
                ((SimpleValue)astObject.args.getFirst()).value.content,
                String.format("invalid argument value of the function call (`%s`)", expr)
        );

        Assertions.assertFalse(
                astObject.isPostfix,
                String.format("the function call must not be postfix call (`%s`)", expr)
        );
    }

    @Test
    void validSingleFuncCallWithTwoArgs() {
        final var name = "validFuncCall";
        final var firstArgValue = "'что-то мега крутое на русском (by avi)'";
        final var secondArgValue = "2.9764";
        final var expr = String.format("%s(%s, %s)", name, firstArgValue, secondArgValue);
        final var context = Utils.parseContextOf(expr);
        final var parseResult = ComplexValueParser.DEFAULT.parse(context);

        Assertions.assertTrue(parseResult.isSuccessful(), "the parse result must be successful");

        Assertions.assertDoesNotThrow(
                parseResult::valueOrThrow,
                "the resulted ast object must be non null"
        );

        Assertions.assertInstanceOf(FuncCallObject.class, parseResult.valueOrThrow());

        final var astObject = (FuncCallObject)parseResult.valueOrThrow();

        Assertions.assertEquals(
                name,
                astObject.name.content,
                String.format("invalid callable function name (`%s`)", expr)
        );

        Assertions.assertEquals(
                2,
                astObject.args.size(),
                String.format("the function call must contain exactly one argument (`%s`)", expr)
        );

        Assertions.assertInstanceOf(
                SimpleValue.class,
                astObject.args.getFirst(),
                String.format("the first argument of the function call must be simple str value (`%s`)", expr)
        );

        Assertions.assertEquals(
                firstArgValue,
                ((SimpleValue)astObject.args.getFirst()).value.content,
                String.format("invalid first argument value of the function call (`%s`)", expr)
        );

        final var secondSimpleValueObject = (SimpleValue)astObject.args.getLast();

        Assertions.assertEquals(
                secondArgValue,
                secondSimpleValueObject.value.content,
                String.format("invalid second argument value of the function call (`%s`)", expr)
        );

        Assertions.assertFalse(
                astObject.isPostfix,
                String.format("the function call must not be postfix call (`%s`)", expr)
        );
    }

    @Test
    void validSingleFuncCallWithTwoArgsWhereEverythingStartsWithNewLine() {
        final var name = "validFuncCall";
        final var firstArgValue = "'что-то мега крутое на русском (by avi)'";
        final var secondArgValue = "2.9764";
        final var expr = String.format("%s\n(\n%s\n,\n%s\n)", name, firstArgValue, secondArgValue);
        final var context = Utils.parseContextOf(expr);
        final var parseResult = ComplexValueParser.DEFAULT.parse(context);

        Assertions.assertTrue(parseResult.isSuccessful(), "the parse result must be successful");

        Assertions.assertDoesNotThrow(
                parseResult::valueOrThrow,
                "the resulted ast object must be non null"
        );

        Assertions.assertInstanceOf(FuncCallObject.class, parseResult.valueOrThrow());

        final var astObject = (FuncCallObject)parseResult.valueOrThrow();

        Assertions.assertEquals(
                name,
                astObject.name.content,
                String.format("invalid callable function name (`%s`)", expr)
        );

        Assertions.assertEquals(
                2,
                astObject.args.size(),
                String.format("the function call must contain exactly one argument (`%s`)", expr)
        );

        Assertions.assertInstanceOf(
                SimpleValue.class,
                astObject.args.getFirst(),
                String.format("the first argument of the function call must be simple str value (`%s`)", expr)
        );

        Assertions.assertEquals(
                firstArgValue,
                ((SimpleValue)astObject.args.getFirst()).value.content,
                String.format("invalid first argument value of the function call (`%s`)", expr)
        );

        Assertions.assertInstanceOf(
                SimpleValue.class,
                astObject.args.getLast(),
                String.format("the second argument of the function call must be simple value (`%s`)", expr)
        );

        final var secondSimpleValueObject = (SimpleValue)astObject.args.getLast();

        Assertions.assertEquals(
                secondArgValue,
                secondSimpleValueObject.value.content,
                String.format("invalid second argument value of the function call (`%s`)", expr)
        );

        Assertions.assertFalse(
                astObject.isPostfix,
                String.format("the function call must not be postfix call (`%s`)", expr)
        );
    }

    @Test
    void validFirstStrValueAndSecondPostfixFuncCallNoRoundBrackets() {
        final var value = "'89'";
        final var funcName = "int";
        final var expr = String.format("%s!%s", value, funcName);
        final var context = Utils.parseContextOf(expr);
        final var parseResult = ComplexValueParser.DEFAULT.parse(context);

        Assertions.assertTrue(parseResult.isSuccessful(), "the parse result must be successful");

        Assertions.assertDoesNotThrow(
                parseResult::valueOrThrow,
                "the resulted ast object must be non null"
        );

        Assertions.assertInstanceOf(ComplexValueObject.class, parseResult.valueOrThrow());

        final var astObject = (ComplexValueObject)parseResult.valueOrThrow();

        Assertions.assertEquals(
                2,
                astObject.segments.size(),
                String.format("`%s` must consists of exactly two segment", expr)
        );

        Assertions.assertInstanceOf(
                SimpleValue.class,
                astObject.segments.getFirst(),
                String.format("`%s` must be simple str value", expr)
        );

        final var valueObject = (SimpleValue)astObject.segments.getFirst();

        Assertions.assertEquals(Simple.STR, valueObject.value.type);

        Assertions.assertEquals(
                value,
                valueObject.value.content,
                String.format("invalid value, expected `%s` as in `%s`", value, expr)
        );

        Assertions.assertInstanceOf(
                FuncCallObject.class,
                astObject.segments.getLast(),
                String.format("`%s` must be postfix function call", expr)
        );

        final var funcCallObject = (FuncCallObject)astObject.segments.getLast();

        Assertions.assertEquals(
                funcName,
                funcCallObject.name.content,
                String.format("invalid postfix function call name (`%s`)", expr)
        );

        Assertions.assertTrue(
                funcCallObject.isPostfix,
                String.format("the function call `%s` should be postfix function call", expr)
        );
    }

    @Test
    void validFirstStrValueAndSecondPostfixFuncCallWithRoundBrackets() {
        final var value = "'89'";
        final var funcName = "int";
        final var expr = String.format("%s!%s()", value, funcName);
        final var context = Utils.parseContextOf(expr);
        final var parseResult = ComplexValueParser.DEFAULT.parse(context);

        Assertions.assertTrue(parseResult.isSuccessful(), "the parse result must be successful");

        Assertions.assertDoesNotThrow(
                parseResult::valueOrThrow,
                "the resulted ast object must be non null"
        );

        Assertions.assertInstanceOf(ComplexValueObject.class, parseResult.valueOrThrow());

        final var astObject = (ComplexValueObject)parseResult.valueOrThrow();

        Assertions.assertEquals(
                2,
                astObject.segments.size(),
                String.format("`%s` must consists of exactly two segment", expr)
        );

        Assertions.assertInstanceOf(
                SimpleValue.class,
                astObject.segments.getFirst(),
                String.format("`%s` must be simple str value", expr)
        );

        final var valueObject = (SimpleValue)astObject.segments.getFirst();

        Assertions.assertEquals(Simple.STR, valueObject.value.type);

        Assertions.assertEquals(
                value,
                valueObject.value.content,
                String.format("invalid value, expected `%s` as in `%s`", value, expr)
        );

        Assertions.assertInstanceOf(
                FuncCallObject.class,
                astObject.segments.getLast(),
                String.format("`%s` must be postfix function call", expr)
        );

        final var funcCallObject = (FuncCallObject)astObject.segments.getLast();

        Assertions.assertEquals(
                funcName,
                funcCallObject.name.content,
                String.format("invalid postfix function call name (`%s`)", expr)
        );

        Assertions.assertTrue(
                funcCallObject.isPostfix,
                String.format("the function call `%s` should be postfix function call", expr)
        );
    }

    @Test
    void validThreeFieldAccessSegmentsWithNewLines() {
        final var expr = "one\n.\ntwo\n.\nthree";
        final var context = Utils.parseContextOf(expr);
        final var parseResult = ComplexValueParser.DEFAULT.parse(context);

        Assertions.assertTrue(parseResult.isSuccessful(), "the parse result must be successful");

        Assertions.assertDoesNotThrow(
                parseResult::valueOrThrow,
                "the resulted ast object must be non null"
        );

        Assertions.assertInstanceOf(ComplexValueObject.class, parseResult.valueOrThrow());

        final var astObject = (ComplexValueObject)parseResult.valueOrThrow();

        Assertions.assertEquals(
                3,
                astObject.segments.size(),
                String.format("`%s` must consists of exactly two segment", Representable.repr(expr, true))
        );
    }

    @Test
    void invalidNoSecondSegmentWithDotAfterFirstSegment() {
        final var expr = "one.";
        final var context = Utils.parseContextOf(expr);
        final var parseResult = ComplexValueParser.DEFAULT.parse(context);

        Assertions.assertTrue(parseResult.isFailed(), "the parse result must be specified as failed");

        Assertions.assertNull(
                parseResult.value(),
                "the resulted ast object must be null"
        );
    }

    @Test
    void invalidFuncCallWithMissedSecondArgument() {
        final var expr = "invalidFuncCall('one',,)";
        final var context = Utils.parseContextOf(expr);
        final var parseResult = ComplexValueParser.DEFAULT.parse(context);

        Assertions.assertTrue(parseResult.isFailed(), "the parse result must be specified as failed");

        Assertions.assertNull(
                parseResult.value(),
                "the resulted ast object must be null"
        );
    }

    @Test
    void invalidFuncCallWithMissedFirstArgument() {
        final var expr = "invalidFuncCall(, 'two',)";
        final var context = Utils.parseContextOf(expr);
        final var parseResult = ComplexValueParser.DEFAULT.parse(context);

        Assertions.assertTrue(parseResult.isFailed(), "the parse result must be specified as failed");

        Assertions.assertNull(
                parseResult.value(),
                "the resulted ast object must be null"
        );
    }

    @Test
    void invalidFuncCallWithArgsNotSeparatedByComma() {
        final var expr = "invalidFuncCall('one' 'two')";
        final var context = Utils.parseContextOf(expr);
        final var parseResult = ComplexValueParser.DEFAULT.parse(context);

        Assertions.assertTrue(parseResult.isFailed(), "the parse result must be specified as failed");

        Assertions.assertNull(
                parseResult.value(),
                "the resulted ast object must be null"
        );
    }

    @Test
    void invalidFuncCallWithMissedClosingRoundBracket() {
        final var expr = "invalidFuncCall(";
        final var context = Utils.parseContextOf(expr);
        final var parseResult = ComplexValueParser.DEFAULT.parse(context);

        Assertions.assertTrue(parseResult.isFailed(), "the parse result must be specified as failed");

        Assertions.assertNull(
                parseResult.value(),
                "the resulted ast object must be null"
        );
    }

    @Test
    void invalidSecondValue() {
        final var expr = "'one'.'two'";
        final var context = Utils.parseContextOf(expr);
        final var parseResult = ComplexValueParser.DEFAULT.parse(context);

        Assertions.assertTrue(parseResult.isFailed(), "the parse result must be specified as failed");

        Assertions.assertNull(
                parseResult.value(),
                "the resulted ast object must be null"
        );
    }

    @Test
    void invalidNoWords() {
        final var expr = "";
        final var context = Utils.parseContextOf(expr);
        final var parseResult = ComplexValueParser.DEFAULT.parse(context);

        Assertions.assertTrue(parseResult.isCanceled(), "the parse result must be specified as canceled");

        Assertions.assertNull(
                parseResult.value(),
                "the resulted ast object must be null"
        );
    }
}