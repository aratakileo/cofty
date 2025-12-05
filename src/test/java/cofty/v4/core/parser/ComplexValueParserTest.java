package cofty.v4.core.parser;

import cofty.Utils;
import cofty.core.lexer.token.type.Simple;
import cofty.type.Representable;
import cofty.v4.core.parser.ast.value.complex.ComplexValueObject;
import cofty.v4.core.parser.ast.value.complex.FieldAccessObject;
import cofty.v4.core.parser.ast.value.complex.FuncCallObject;
import cofty.v4.core.parser.ast.value.complex.SimpleValue;
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

        final var astObject = parseResult.valueOrThrow();

        Assertions.assertInstanceOf(
                FieldAccessObject.class,
                astObject.segments.getFirst(),
                String.format("`%s` must be field access", expr)
        );

        Assertions.assertEquals(
                1,
                astObject.segments.size(),
                String.format("`%s` must contain exactly one segment", expr)
        );

        final var valueObject = (FieldAccessObject)astObject.segments.getFirst();

        Assertions.assertEquals(
                "validField",
                valueObject.name.content,
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

        final var astObject = parseResult.valueOrThrow();

        Assertions.assertInstanceOf(
                SimpleValue.class,
                astObject.segments.getFirst(),
                String.format("`%s` must be simple int value", expr)
        );

        Assertions.assertEquals(
                1,
                astObject.segments.size(),
                String.format("`%s` must contain exactly one segment", expr)
        );

        final var valueObject = (SimpleValue)astObject.segments.getFirst();

        Assertions.assertEquals(Simple.INT, valueObject.value.type);

        Assertions.assertEquals(
                "51",
                valueObject.value.content,
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

        final var astObject = parseResult.valueOrThrow();

        Assertions.assertInstanceOf(
                FuncCallObject.class,
                astObject.segments.getFirst(),
                String.format("`%s` must be function call", expr)
        );

        Assertions.assertEquals(
                1,
                astObject.segments.size(),
                String.format("`%s` must contain exactly one segment", expr)
        );

        final var valueObject = (FuncCallObject)astObject.segments.getFirst();

        Assertions.assertEquals(
                name,
                valueObject.name.content,
                String.format("invalid callable function name (`%s`)", expr)
        );

        Assertions.assertTrue(
                valueObject.args.isEmpty(),
                String.format("the function call must contain no arguments (`%s`)", expr)
        );

        Assertions.assertFalse(
                valueObject.isPostfix,
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

        final var astObject = parseResult.valueOrThrow();

        Assertions.assertInstanceOf(
                FuncCallObject.class,
                astObject.segments.getFirst(),
                String.format("`%s` must be function call", expr)
        );

        Assertions.assertEquals(
                1,
                astObject.segments.size(),
                String.format("`%s` must contain exactly one segment", expr)
        );

        final var valueObject = (FuncCallObject)astObject.segments.getFirst();

        Assertions.assertEquals(
                name,
                valueObject.name.content,
                String.format("invalid callable function name (`%s`)", expr)
        );

        Assertions.assertEquals(
                1,
                valueObject.args.size(),
                String.format("the function call must contain exactly one argument (`%s`)", expr)
        );

        Assertions.assertInstanceOf(
                ComplexValueObject.class,
                valueObject.args.getFirst().expr,
                String.format("the argument of the function call must be complex value (`%s`)", expr)
        );

        final var complexValueObject = (ComplexValueObject)valueObject.args.getFirst().expr;

        Assertions.assertInstanceOf(
                SimpleValue.class,
                complexValueObject.segments.getFirst(),
                String.format("the argument of the function call must be simple str value (`%s`)", expr)
        );

        Assertions.assertEquals(
                argValue,
                ((SimpleValue)complexValueObject.segments.getFirst()).value.content,
                String.format("invalid argument value of the function call (`%s`)", expr)
        );

        Assertions.assertFalse(
                valueObject.isPostfix,
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

        final var astObject = parseResult.valueOrThrow();

        Assertions.assertInstanceOf(
                FuncCallObject.class,
                astObject.segments.getFirst(),
                String.format("`%s` must be function call", expr)
        );

        Assertions.assertEquals(
                1,
                astObject.segments.size(),
                String.format("`%s` must contain exactly one segment", expr)
        );

        final var valueObject = (FuncCallObject)astObject.segments.getFirst();

        Assertions.assertEquals(
                name,
                valueObject.name.content,
                String.format("invalid callable function name (`%s`)", expr)
        );

        Assertions.assertEquals(
                2,
                valueObject.args.size(),
                String.format("the function call must contain exactly one argument (`%s`)", expr)
        );

        Assertions.assertInstanceOf(
                ComplexValueObject.class,
                valueObject.args.getFirst().expr,
                String.format("the first argument of the function call must be complex value (`%s`)", expr)
        );

        final var firstComplexValueObject = (ComplexValueObject)valueObject.args.getFirst().expr;

        Assertions.assertInstanceOf(
                SimpleValue.class,
                firstComplexValueObject.segments.getFirst(),
                String.format("the first argument of the function call must be simple str value (`%s`)", expr)
        );

        Assertions.assertEquals(
                firstArgValue,
                ((SimpleValue)firstComplexValueObject.segments.getFirst()).value.content,
                String.format("invalid first argument value of the function call (`%s`)", expr)
        );

        Assertions.assertInstanceOf(
                ComplexValueObject.class,
                valueObject.args.getLast().expr,
                String.format("the second argument of the function call must be complex value (`%s`)", expr)
        );

        final var secondComplexValueObject = (ComplexValueObject)valueObject.args.getLast().expr;

        Assertions.assertInstanceOf(
                SimpleValue.class,
                secondComplexValueObject.segments.getFirst(),
                String.format("the second argument of the function call must be simple double value (`%s`)", expr)
        );

        Assertions.assertEquals(
                secondArgValue,
                ((SimpleValue)secondComplexValueObject.segments.getFirst()).value.content,
                String.format("invalid second argument value of the function call (`%s`)", expr)
        );

        Assertions.assertFalse(
                valueObject.isPostfix,
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

        final var astObject = parseResult.valueOrThrow();

        Assertions.assertInstanceOf(
                FuncCallObject.class,
                astObject.segments.getFirst(),
                String.format("`%s` must be function call", expr)
        );

        Assertions.assertEquals(
                1,
                astObject.segments.size(),
                String.format("`%s` must contain exactly one segment", expr)
        );

        final var valueObject = (FuncCallObject)astObject.segments.getFirst();

        Assertions.assertEquals(
                name,
                valueObject.name.content,
                String.format("invalid callable function name (`%s`)", expr)
        );

        Assertions.assertEquals(
                2,
                valueObject.args.size(),
                String.format("the function call must contain exactly one argument (`%s`)", expr)
        );

        Assertions.assertInstanceOf(
                ComplexValueObject.class,
                valueObject.args.getFirst().expr,
                String.format("the first argument of the function call must be complex value (`%s`)", expr)
        );

        final var firstComplexValueObject = (ComplexValueObject)valueObject.args.getFirst().expr;

        Assertions.assertInstanceOf(
                SimpleValue.class,
                firstComplexValueObject.segments.getFirst(),
                String.format("the first argument of the function call must be simple str value (`%s`)", expr)
        );

        Assertions.assertEquals(
                firstArgValue,
                ((SimpleValue)firstComplexValueObject.segments.getFirst()).value.content,
                String.format("invalid first argument value of the function call (`%s`)", expr)
        );

        Assertions.assertInstanceOf(
                ComplexValueObject.class,
                valueObject.args.getLast().expr,
                String.format("the second argument of the function call must be complex value (`%s`)", expr)
        );

        final var secondComplexValueObject = (ComplexValueObject)valueObject.args.getLast().expr;

        Assertions.assertInstanceOf(
                SimpleValue.class,
                secondComplexValueObject.segments.getFirst(),
                String.format("the second argument of the function call must be simple double value (`%s`)", expr)
        );

        Assertions.assertEquals(
                secondArgValue,
                ((SimpleValue)secondComplexValueObject.segments.getFirst()).value.content,
                String.format("invalid second argument value of the function call (`%s`)", expr)
        );

        Assertions.assertFalse(
                valueObject.isPostfix,
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

        final var astObject = parseResult.valueOrThrow();

        Assertions.assertEquals(
                2,
                astObject.segments.size(),
                String.format("`%s` must contain exactly two segment", expr)
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

        final var astObject = parseResult.valueOrThrow();

        Assertions.assertEquals(
                2,
                astObject.segments.size(),
                String.format("`%s` must contain exactly two segment", expr)
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

        final var astObject = parseResult.valueOrThrow();

        Assertions.assertEquals(
                3,
                astObject.segments.size(),
                String.format("`%s` must contain exactly two segment", Representable.repr(expr, true))
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