package cofty.core.parser;

import cofty.core.compiler.diagnostic.Errors;
import cofty.core.lexer.token.type.Simple;
import cofty.core.parser.ast.value.BinaryExpressionObject;
import cofty.core.parser.ast.value.UnaryExpressionObject;
import cofty.core.parser.ast.value.complex.SimpleValueObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ValueExpressionParserTest {
    public final static ValueExpressionParser NEW_LINES_SENSITIVE = ValueExpressionParser.create(true),
            NEW_LINES_INSENSITIVE = ValueExpressionParser.create(false);

    @Test
    void validBinaryExpressionWithComplexNotInOperatorInMiddle() {
        final var leftValue = "'erm'";
        final var rightValue = "'Hello world!'";
        final var operator = "not in";
        final var expr = String.format("%s %s %s", leftValue, operator, rightValue);
        final var astObject = ParseResultAssert.parse(expr, NEW_LINES_SENSITIVE)
                .ok()
                .hasNoDiagnosticMessages()
                .value(BinaryExpressionObject.class);

        Assertions.assertEquals(
                2,
                astObject.operators.size(),
                String.format("the operator in `%s` must consists of exactly two parts", expr)
        );

        final var operatorParts = operator.split(" +");

        Assertions.assertEquals(
                operatorParts[0],
                astObject.operators.getFirst().content,
                "invalid first part of operator"
        );

        Assertions.assertEquals(
                operatorParts[1],
                astObject.operators.getLast().content,
                "invalid last part of operator"
        );

        Assertions.assertInstanceOf(SimpleValueObject.class, astObject.leftValue);
        Assertions.assertInstanceOf(SimpleValueObject.class, astObject.rightValue);

        Assertions.assertEquals(
                leftValue,
                ((SimpleValueObject)astObject.leftValue).value.content,
                "invalid expression left value"
        );

        Assertions.assertEquals(
                rightValue,
                ((SimpleValueObject)astObject.rightValue).value.content,
                "invalid expression right value"
        );
    }

    @Test
    void validBinaryExpressionWithSensitivePlusOperatorInMiddle() {
        final var leftValue = "55";
        final var rightValue = "77";
        final var operator = "+";
        final var expr = String.format("%s %s %s", leftValue, operator, rightValue);
        final var astObject = ParseResultAssert.parse(expr, NEW_LINES_SENSITIVE)
                .ok()
                .hasNoDiagnosticMessages()
                .value(BinaryExpressionObject.class);

        Assertions.assertEquals(
                1,
                astObject.operators.size(),
                String.format("the operator in `%s` must consists of exactly one part", expr)
        );

        Assertions.assertEquals(
                operator,
                astObject.operators.getFirst().content,
                "invalid operator"
        );

        Assertions.assertInstanceOf(SimpleValueObject.class, astObject.leftValue);
        Assertions.assertInstanceOf(SimpleValueObject.class, astObject.rightValue);

        Assertions.assertEquals(
                leftValue,
                ((SimpleValueObject)astObject.leftValue).value.content,
                "invalid expression left value"
        );

        Assertions.assertEquals(
                rightValue,
                ((SimpleValueObject)astObject.rightValue).value.content,
                "invalid expression right value"
        );
    }

    @Test
    void validBinaryExpressionWhereEverythingStartsWithNewLine() {
        final var leftValue = "9.9";
        final var rightValue = "0.1";
        final var operator = "*";
        final var expr = String.format("%s\n%s\n%s", leftValue, operator, rightValue);
        final var astObject = ParseResultAssert.parse(expr, NEW_LINES_INSENSITIVE)
                .ok()
                .hasNoDiagnosticMessages()
                .value(BinaryExpressionObject.class);

        Assertions.assertEquals(
                1,
                astObject.operators.size(),
                String.format("the operator in `%s` must consists of exactly one part", expr)
        );

        Assertions.assertEquals(
                operator,
                astObject.operators.getFirst().content,
                "invalid operator"
        );

        Assertions.assertInstanceOf(SimpleValueObject.class, astObject.leftValue);
        Assertions.assertInstanceOf(SimpleValueObject.class, astObject.rightValue);

        Assertions.assertEquals(
                leftValue,
                ((SimpleValueObject)astObject.leftValue).value.content,
                "invalid expression left value"
        );

        Assertions.assertEquals(
                rightValue,
                ((SimpleValueObject)astObject.rightValue).value.content,
                "invalid expression right value"
        );
    }

    @Test
    void validUnaryExpressionWithSensitiveMinusOperator() {
        final var value = "69";
        final var operator = "-";
        final var expr = String.format("%s%s", operator, value);
        final var astObject = ParseResultAssert.parse(expr, NEW_LINES_SENSITIVE)
                .ok()
                .hasNoDiagnosticMessages()
                .value(UnaryExpressionObject.class);

        Assertions.assertEquals(
                operator,
                astObject.operator.content,
                "invalid operator"
        );

        Assertions.assertInstanceOf(SimpleValueObject.class, astObject.value);

        Assertions.assertEquals(
                value,
                ((SimpleValueObject)astObject.value).value.content,
                "invalid expression value"
        );
    }

    @Test
    void validLeftAssociativeHigherPriorityWithBrackets() {
        final var firstValue = "55";
        final var middleValue = "77";
        final var lastValue = "69";
        final var firstOperator = "-";
        final var lastOperator = "*";
        final var expr = String.format("(%s %s %s) %s %s", firstValue, firstOperator, middleValue, lastOperator, lastValue);
        final var firstOperatorExpressionObject = ParseResultAssert.parse(expr, NEW_LINES_SENSITIVE)
                .ok()
                .hasNoDiagnosticMessages()
                .value(BinaryExpressionObject.class);

        Assertions.assertEquals(
                1,
                firstOperatorExpressionObject.operators.size(),
                String.format("the operator in `%s` must consists of exactly one part", expr)
        );

        Assertions.assertEquals(
                lastOperator,
                firstOperatorExpressionObject.operators.getFirst().content,
                "invalid last operator"
        );

        Assertions.assertInstanceOf(BinaryExpressionObject.class, firstOperatorExpressionObject.leftValue);
        Assertions.assertInstanceOf(SimpleValueObject.class, firstOperatorExpressionObject.rightValue);

        Assertions.assertEquals(
                lastValue,
                ((SimpleValueObject)firstOperatorExpressionObject.rightValue).value.content,
                "invalid expression last value"
        );

        final var lastOperatorExpressionObject = (BinaryExpressionObject)firstOperatorExpressionObject.leftValue;

        Assertions.assertEquals(
                1,
                lastOperatorExpressionObject.operators.size(),
                String.format("the operator in `%s` must consists of exactly one part", expr)
        );

        Assertions.assertEquals(
                firstOperator,
                lastOperatorExpressionObject.operators.getFirst().content,
                "invalid first operator"
        );

        Assertions.assertInstanceOf(SimpleValueObject.class, lastOperatorExpressionObject.leftValue);
        Assertions.assertInstanceOf(SimpleValueObject.class, lastOperatorExpressionObject.rightValue);

        Assertions.assertEquals(
                firstValue,
                ((SimpleValueObject)lastOperatorExpressionObject.leftValue).value.content,
                "invalid expression first value"
        );

        Assertions.assertEquals(
                middleValue,
                ((SimpleValueObject)lastOperatorExpressionObject.rightValue).value.content,
                "invalid expression middle value"
        );
    }

    @Test
    void validLeftAssociativeHigherPriorityBinaryOperator() {
        final var firstValue = "55";
        final var middleValue = "77";
        final var lastValue = "69";
        final var firstOperator = "-";
        final var lastOperator = "*";
        final var expr = String.format("%s %s %s %s %s", firstValue, firstOperator, middleValue, lastOperator, lastValue);
        final var firstOperatorExpressionObject = ParseResultAssert.parse(expr, NEW_LINES_SENSITIVE)
                .ok()
                .hasNoDiagnosticMessages()
                .value(BinaryExpressionObject.class);

        Assertions.assertEquals(
                1,
                firstOperatorExpressionObject.operators.size(),
                String.format("the operator in `%s` must consists of exactly one part", expr)
        );

        Assertions.assertEquals(
                firstOperator,
                firstOperatorExpressionObject.operators.getFirst().content,
                "invalid first operator"
        );

        Assertions.assertInstanceOf(SimpleValueObject.class, firstOperatorExpressionObject.leftValue);
        Assertions.assertInstanceOf(BinaryExpressionObject.class, firstOperatorExpressionObject.rightValue);

        Assertions.assertEquals(
                firstValue,
                ((SimpleValueObject)firstOperatorExpressionObject.leftValue).value.content,
                "invalid expression first value"
        );

        final var lastOperatorExpressionObject = (BinaryExpressionObject)firstOperatorExpressionObject.rightValue;

        Assertions.assertEquals(
                1,
                lastOperatorExpressionObject.operators.size(),
                String.format("the operator in `%s` must consists of exactly one part", expr)
        );

        Assertions.assertEquals(
                lastOperator,
                lastOperatorExpressionObject.operators.getFirst().content,
                "invalid last operator"
        );

        Assertions.assertInstanceOf(SimpleValueObject.class, lastOperatorExpressionObject.leftValue);
        Assertions.assertInstanceOf(SimpleValueObject.class, lastOperatorExpressionObject.rightValue);

        Assertions.assertEquals(
                middleValue,
                ((SimpleValueObject)lastOperatorExpressionObject.leftValue).value.content,
                "invalid expression middle value"
        );

        Assertions.assertEquals(
                lastValue,
                ((SimpleValueObject)lastOperatorExpressionObject.rightValue).value.content,
                "invalid expression last value"
        );
    }

    @Test
    void validRightAssociativeHigherPriorityBinaryOperator() {
        final var firstValue = "55";
        final var middleValue = "77";
        final var lastValue = "69";
        final var firstOperator = "**";
        final var lastOperator = "*";
        final var expr = String.format("%s %s %s %s %s", firstValue, firstOperator, middleValue, lastOperator, lastValue);
        final var firstOperatorExpressionObject = ParseResultAssert.parse(expr, NEW_LINES_SENSITIVE)
                .ok()
                .hasNoDiagnosticMessages()
                .value(BinaryExpressionObject.class);

        Assertions.assertEquals(
                1,
                firstOperatorExpressionObject.operators.size(),
                String.format("the operator in `%s` must consists of exactly one part", expr)
        );

        Assertions.assertEquals(
                lastOperator,
                firstOperatorExpressionObject.operators.getFirst().content,
                "invalid last operator"
        );

        Assertions.assertInstanceOf(BinaryExpressionObject.class, firstOperatorExpressionObject.leftValue);
        Assertions.assertInstanceOf(SimpleValueObject.class, firstOperatorExpressionObject.rightValue);

        Assertions.assertEquals(
                lastValue,
                ((SimpleValueObject)firstOperatorExpressionObject.rightValue).value.content,
                "invalid expression last value"
        );

        final var lastOperatorExpressionObject = (BinaryExpressionObject)firstOperatorExpressionObject.leftValue;

        Assertions.assertEquals(
                1,
                lastOperatorExpressionObject.operators.size(),
                String.format("the operator in `%s` must consists of exactly one part", expr)
        );

        Assertions.assertEquals(
                firstOperator,
                lastOperatorExpressionObject.operators.getFirst().content,
                "invalid first operator"
        );

        Assertions.assertInstanceOf(SimpleValueObject.class, lastOperatorExpressionObject.leftValue);
        Assertions.assertInstanceOf(SimpleValueObject.class, lastOperatorExpressionObject.rightValue);

        Assertions.assertEquals(
                firstValue,
                ((SimpleValueObject)lastOperatorExpressionObject.leftValue).value.content,
                "invalid expression first value"
        );

        Assertions.assertEquals(
                middleValue,
                ((SimpleValueObject)lastOperatorExpressionObject.rightValue).value.content,
                "invalid expression middle value"
        );
    }

    @Test
    void invalidBinaryExpressionWhereEverythingStartsWithNewLine() {
        final var leftValue = "9.9";
        final var expr = String.format("%s\n*\n0.1\n/\n5\n**\n100", leftValue);
        final var parseResultAssert = ParseResultAssert.parse(expr, NEW_LINES_SENSITIVE)
                .ok()
                .hasNoDiagnosticMessages();

        final var astObject = parseResultAssert.value(SimpleValueObject.class);

        Assertions.assertEquals(leftValue, astObject.value.content);
        Assertions.assertNotNull(parseResultAssert.context.current());
        Assertions.assertEquals(Simple.NEWLINE, parseResultAssert.context.currentOrThrow().type);
    }

    @Test
    void invalidNeverClosedRoundBrackets() {
        ParseResultAssert.parse("(6969", NEW_LINES_SENSITIVE)
                .failed()
                .hasErrors(Errors.UNCLOSED_PARENTHESIS);
    }

    @Test
    void invalidEmptyBrackets() {
        ParseResultAssert.parse("()", NEW_LINES_SENSITIVE)
                .failed()
                .hasErrors(Errors.EXPECTED_OPERAND_NOT_CLOSING_PARENT);
    }

    @Test
    void invalidClosingBracketOnOperandPLace() {
        ParseResultAssert.parse("(6969 / )", NEW_LINES_SENSITIVE)
                .failed()
                .hasErrors(Errors.EXPECTED_OPERAND_NOT_CLOSING_PARENT);
    }

    @Test
    void invalidNoOperandForUnaryOperator() {
        ParseResultAssert.parse("not", NEW_LINES_SENSITIVE)
                .failed()
                .hasErrors(Errors.EXPECTED_OPERAND);
    }

    @Test
    void invalidNoSecondOperandForBinaryOperator() {
        ParseResultAssert.parse("6969 /", NEW_LINES_SENSITIVE)
                .failed()
                .hasErrors(Errors.EXPECTED_OPERAND);
    }

    @Test
    void invalidUnaryOperatorOnBinaryOperatorPlace() {
        ParseResultAssert.parse("6969 not", NEW_LINES_SENSITIVE)
                .failed()
                .hasErrors(Errors.INVALID_OPERATOR_FOR_CONTEXT);
    }

    @Test
    void invalidBinaryOperatorOnUnaryOperatorPlace() {
        ParseResultAssert.parse("is 6969", NEW_LINES_SENSITIVE)
                .failed()
                .hasErrors(Errors.INVALID_OPERATOR_FOR_CONTEXT);
    }

    @Test
    void invalidOpeningBracketOnBinaryOperatorPlace() {
        ParseResultAssert.parse("6969()", NEW_LINES_SENSITIVE)
                .failed()
                .hasErrors(Errors.EXPECTED_BINARY_OPERATOR);
    }
}