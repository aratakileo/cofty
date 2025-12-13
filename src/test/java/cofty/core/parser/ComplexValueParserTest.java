package cofty.core.parser;

import cofty.core.compiler.diagnostic.Errors;
import cofty.core.compiler.diagnostic.Warnings;
import cofty.core.lexer.token.type.Simple;
import cofty.type.Representable;
import cofty.core.parser.ast.value.complex.ComplexValueObject;
import cofty.core.parser.ast.value.complex.FieldAccessObject;
import cofty.core.parser.ast.value.complex.FuncCallObject;
import cofty.core.parser.ast.value.complex.SimpleValueObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ComplexValueParserTest {
    @Test
    void validSingleFieldAccess() {
        final var expr = "validField";
        final var astObject = ParseResultAssert.parse(expr, ComplexValueParser.DEFAULT)
                .ok()
                .hasNoDiagnosticMessages()
                .value(FieldAccessObject.class);

        Assertions.assertEquals(
                "validField",
                astObject.name.content,
                String.format("invalid field name (`%s`)", expr)
        );
    }

    @Test
    void validSingleIntValue() {
        final var expr = "51";
        final var astObject = ParseResultAssert.parse(expr, ComplexValueParser.DEFAULT)
                .ok()
                .hasNoDiagnosticMessages()
                .value(SimpleValueObject.class);

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
        final var astObject = ParseResultAssert.parse(expr, ComplexValueParser.DEFAULT)
                .ok()
                .hasNoDiagnosticMessages()
                .value(FuncCallObject.class);

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
        final var astObject = ParseResultAssert.parse(expr, ComplexValueParser.DEFAULT)
                .ok()
                .hasNoDiagnosticMessages()
                .value(FuncCallObject.class);

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
                SimpleValueObject.class,
                astObject.args.getFirst(),
                String.format("the argument of the function call must be simple str value (`%s`)", expr)
        );

        Assertions.assertEquals(
                argValue,
                ((SimpleValueObject)astObject.args.getFirst()).value.content,
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
        final var astObject = ParseResultAssert.parse(expr, ComplexValueParser.DEFAULT)
                .ok()
                .hasNoDiagnosticMessages()
                .value(FuncCallObject.class);

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
                SimpleValueObject.class,
                astObject.args.getFirst(),
                String.format("the first argument of the function call must be simple str value (`%s`)", expr)
        );

        Assertions.assertEquals(
                firstArgValue,
                ((SimpleValueObject)astObject.args.getFirst()).value.content,
                String.format("invalid first argument value of the function call (`%s`)", expr)
        );

        final var secondSimpleValueObject = (SimpleValueObject)astObject.args.getLast();

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
        final var expr = "%s\n(\n%s\n,\n%s\n)".formatted(name, firstArgValue, secondArgValue);
        final var astObject = ParseResultAssert.parse(expr, ComplexValueParser.DEFAULT)
                .ok()
                .hasNoDiagnosticMessages()
                .value(FuncCallObject.class);

        Assertions.assertEquals(
                name,
                astObject.name.content,
                String.format("invalid callable function name (`%s`)", expr)
        );

        Assertions.assertEquals(
                2,
                astObject.args.size(),
                "the function call must contain exactly two arguments (`%s`)".formatted(expr)
        );

        Assertions.assertInstanceOf(
                SimpleValueObject.class,
                astObject.args.getFirst(),
                String.format("the first argument of the function call must be simple str value (`%s`)", expr)
        );

        Assertions.assertEquals(
                firstArgValue,
                ((SimpleValueObject)astObject.args.getFirst()).value.content,
                String.format("invalid first argument value of the function call (`%s`)", expr)
        );

        Assertions.assertInstanceOf(
                SimpleValueObject.class,
                astObject.args.getLast(),
                String.format("the second argument of the function call must be simple value (`%s`)", expr)
        );

        final var secondSimpleValueObject = (SimpleValueObject)astObject.args.getLast();

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
        final var astObject = ParseResultAssert.parse(expr, ComplexValueParser.DEFAULT)
                .ok()
                .hasNoDiagnosticMessages()
                .value(ComplexValueObject.class);

        Assertions.assertEquals(
                2,
                astObject.segments.size(),
                String.format("`%s` must consists of exactly two segment", expr)
        );

        Assertions.assertInstanceOf(
                SimpleValueObject.class,
                astObject.segments.getFirst(),
                String.format("`%s` must be simple str value", expr)
        );

        final var valueObject = (SimpleValueObject)astObject.segments.getFirst();

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
        final var astObject = ParseResultAssert.parse(expr, ComplexValueParser.DEFAULT)
                .ok()
                .hasNoErrors()
                .hasWarnings(Warnings.POSTFIX_FUNC_CALL_WITH_NO_ARGS)
                .value(ComplexValueObject.class);

        Assertions.assertEquals(
                2,
                astObject.segments.size(),
                String.format("`%s` must consists of exactly two segment", expr)
        );

        Assertions.assertInstanceOf(
                SimpleValueObject.class,
                astObject.segments.getFirst(),
                String.format("`%s` must be simple str value", expr)
        );

        final var valueObject = (SimpleValueObject)astObject.segments.getFirst();

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
        final var astObject = ParseResultAssert.parse(expr, ComplexValueParser.DEFAULT)
                .ok()
                .hasNoDiagnosticMessages()
                .value(ComplexValueObject.class);

        Assertions.assertEquals(
                3,
                astObject.segments.size(),
                String.format("`%s` must consists of exactly three segment", Representable.repr(expr, true))
        );
    }

    @Test
    void validTooLongPostfixChain() {
        final var expr = "value!one!two!three!four"; // more than three postfix calls
        final var astObject = ParseResultAssert.parse(expr, ComplexValueParser.DEFAULT)
                .ok()
                .hasNoErrors()
                .hasWarnings(Warnings.LONG_POSTFIX_CHAIN)
                .value(ComplexValueObject.class);

        Assertions.assertEquals(
                5,
                astObject.segments.size(),
                String.format("`%s` must consists of exactly five segment", Representable.repr(expr, true))
        );
    }

    @Test
    void invalidNoSecondSegmentWithDotAfterFirstSegment() {
        ParseResultAssert.parse("one.", ComplexValueParser.DEFAULT)
                .failed()
                .hasErrors(Errors.EXPECTED_MEMBER_ACCESS);
    }

    @Test
    void invalidFuncCallWithMissedSecondArgument() {
        ParseResultAssert.parse("invalidFuncCall('one',,)", ComplexValueParser.DEFAULT)
                .failed()
                .hasErrors(Errors.UNEXPECTED_SEPARATOR);
    }

    @Test
    void invalidFuncCallWithMissedFirstArgument() {
        ParseResultAssert.parse("invalidFuncCall(, 'two',)", ComplexValueParser.DEFAULT)
                .failed()
                .hasErrors(Errors.UNEXPECTED_SEPARATOR);
    }

    @Test
    void invalidFuncCallWithArgsNotSeparatedByComma() {
        ParseResultAssert.parse("invalidFuncCall('one' 'two')", ComplexValueParser.DEFAULT)
                .failed()
                .hasErrors(Errors.MISSING_SEPARATOR);
    }

    @Test
    void invalidFuncCallWithMissedClosingRoundBracket() {
        ParseResultAssert.parse("invalidFuncCall(", ComplexValueParser.DEFAULT)
                .failed()
                .hasErrors(Errors.UNCLOSED_BRACKETS);
    }

    @Test
    void invalidSecondValue() {
        ParseResultAssert.parse("'one'.'two'", ComplexValueParser.DEFAULT)
                .failed()
                .hasErrors(Errors.EXPECTED_MEMBER_ACCESS);
    }

    @Test
    void invalidNoWords() {
        ParseResultAssert.parse("", ComplexValueParser.DEFAULT).skipped().hasNoDiagnosticMessages();
    }
}