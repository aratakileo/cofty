package cofty.v4.core.parser;

import cofty.Utils;
import cofty.core.lexer.token.type.Simple;
import cofty.v4.core.parser.ast.value.BinaryExpressionObject;
import cofty.v4.core.parser.ast.value.UnaryExpressionObject;
import cofty.v4.core.parser.ast.value.complex.SimpleValue;
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
        final var context = Utils.parseContextOf(expr);
        final var parseResult = NEW_LINES_SENSITIVE.parse(context);

        Assertions.assertTrue(parseResult.isSuccessful(), "the parse result must be successful");

        Assertions.assertDoesNotThrow(
                parseResult::valueOrThrow,
                "the resulted ast object must be non null"
        );

        Assertions.assertInstanceOf(
                BinaryExpressionObject.class,
                parseResult.valueOrThrow(),
                String.format("the resulted ast object must be the binary expression object (`%s`)", expr)
        );

        final var astObject = (BinaryExpressionObject)parseResult.valueOrThrow();

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

        Assertions.assertInstanceOf(SimpleValue.class, astObject.leftValue);
        Assertions.assertInstanceOf(SimpleValue.class, astObject.rightValue);

        Assertions.assertEquals(
                leftValue,
                ((SimpleValue)astObject.leftValue).value.content,
                "invalid expression left value"
        );

        Assertions.assertEquals(
                rightValue,
                ((SimpleValue)astObject.rightValue).value.content,
                "invalid expression right value"
        );
    }

    @Test
    void validBinaryExpressionWithSensitivePlusOperatorInMiddle() {
        final var leftValue = "55";
        final var rightValue = "77";
        final var operator = "+";
        final var expr = String.format("%s %s %s", leftValue, operator, rightValue);
        final var context = Utils.parseContextOf(expr);
        final var parseResult = NEW_LINES_SENSITIVE.parse(context);

        Assertions.assertTrue(parseResult.isSuccessful(), "the parse result must be successful");

        Assertions.assertDoesNotThrow(
                parseResult::valueOrThrow,
                "the resulted ast object must be non null"
        );

        Assertions.assertInstanceOf(
                BinaryExpressionObject.class,
                parseResult.valueOrThrow(),
                String.format("the resulted ast object must be the binary expression object (`%s`)", expr)
        );

        final var astObject = (BinaryExpressionObject)parseResult.valueOrThrow();

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

        Assertions.assertInstanceOf(SimpleValue.class, astObject.leftValue);
        Assertions.assertInstanceOf(SimpleValue.class, astObject.rightValue);

        Assertions.assertEquals(
                leftValue,
                ((SimpleValue)astObject.leftValue).value.content,
                "invalid expression left value"
        );

        Assertions.assertEquals(
                rightValue,
                ((SimpleValue)astObject.rightValue).value.content,
                "invalid expression right value"
        );
    }

    @Test
    void validBinaryExpressionWhereEverythingStartsWithNewLine() {
        final var leftValue = "9.9";
        final var rightValue = "0.1";
        final var operator = "*";
        final var expr = String.format("%s\n%s\n%s", leftValue, operator, rightValue);
        final var context = Utils.parseContextOf(expr);
        final var parseResult = NEW_LINES_INSENSITIVE.parse(context);

        Assertions.assertTrue(parseResult.isSuccessful(), "the parse result must be successful");

        Assertions.assertDoesNotThrow(
                parseResult::valueOrThrow,
                "the resulted ast object must be non null"
        );

        Assertions.assertInstanceOf(
                BinaryExpressionObject.class,
                parseResult.valueOrThrow(),
                String.format("the resulted ast object must be the binary expression object (`%s`)", expr)
        );

        final var astObject = (BinaryExpressionObject)parseResult.valueOrThrow();

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

        Assertions.assertInstanceOf(SimpleValue.class, astObject.leftValue);
        Assertions.assertInstanceOf(SimpleValue.class, astObject.rightValue);

        Assertions.assertEquals(
                leftValue,
                ((SimpleValue)astObject.leftValue).value.content,
                "invalid expression left value"
        );

        Assertions.assertEquals(
                rightValue,
                ((SimpleValue)astObject.rightValue).value.content,
                "invalid expression right value"
        );
    }

    @Test
    void validUnaryExpressionWithSensitiveMinusOperator() {
        final var value = "69";
        final var operator = "-";
        final var expr = String.format("%s%s", operator, value);
        final var context = Utils.parseContextOf(expr);
        final var parseResult = NEW_LINES_SENSITIVE.parse(context);

        Assertions.assertTrue(parseResult.isSuccessful(), "the parse result must be successful");

        Assertions.assertDoesNotThrow(
                parseResult::valueOrThrow,
                "the resulted ast object must be non null"
        );

        Assertions.assertInstanceOf(
                UnaryExpressionObject.class,
                parseResult.valueOrThrow(),
                String.format("the resulted ast object must be the binary expression object (`%s`)", expr)
        );

        final var astObject = (UnaryExpressionObject)parseResult.valueOrThrow();

        Assertions.assertEquals(
                operator,
                astObject.operator.content,
                "invalid operator"
        );

        Assertions.assertInstanceOf(SimpleValue.class, astObject.value);

        Assertions.assertEquals(
                value,
                ((SimpleValue)astObject.value).value.content,
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
        final var context = Utils.parseContextOf(expr);
        final var parseResult = NEW_LINES_SENSITIVE.parse(context);

        Assertions.assertTrue(parseResult.isSuccessful(), "the parse result must be successful");

        Assertions.assertDoesNotThrow(
                parseResult::valueOrThrow,
                "the resulted ast object must be non null"
        );

        Assertions.assertInstanceOf(
                BinaryExpressionObject.class,
                parseResult.valueOrThrow(),
                String.format("the resulted ast object must be the binary expression object (`%s`)", expr)
        );

        final var firstOperatorExpressionObject = (BinaryExpressionObject)parseResult.valueOrThrow();

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
        Assertions.assertInstanceOf(SimpleValue.class, firstOperatorExpressionObject.rightValue);

        Assertions.assertEquals(
                lastValue,
                ((SimpleValue)firstOperatorExpressionObject.rightValue).value.content,
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

        Assertions.assertInstanceOf(SimpleValue.class, lastOperatorExpressionObject.leftValue);
        Assertions.assertInstanceOf(SimpleValue.class, lastOperatorExpressionObject.rightValue);

        Assertions.assertEquals(
                firstValue,
                ((SimpleValue)lastOperatorExpressionObject.leftValue).value.content,
                "invalid expression first value"
        );

        Assertions.assertEquals(
                middleValue,
                ((SimpleValue)lastOperatorExpressionObject.rightValue).value.content,
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
        final var context = Utils.parseContextOf(expr);
        final var parseResult = NEW_LINES_SENSITIVE.parse(context);

        Assertions.assertTrue(parseResult.isSuccessful(), "the parse result must be successful");

        Assertions.assertDoesNotThrow(
                parseResult::valueOrThrow,
                "the resulted ast object must be non null"
        );

        Assertions.assertInstanceOf(
                BinaryExpressionObject.class,
                parseResult.valueOrThrow(),
                String.format("the resulted ast object must be the binary expression object (`%s`)", expr)
        );

        final var firstOperatorExpressionObject = (BinaryExpressionObject)parseResult.valueOrThrow();

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

        Assertions.assertInstanceOf(SimpleValue.class, firstOperatorExpressionObject.leftValue);
        Assertions.assertInstanceOf(BinaryExpressionObject.class, firstOperatorExpressionObject.rightValue);

        Assertions.assertEquals(
                firstValue,
                ((SimpleValue)firstOperatorExpressionObject.leftValue).value.content,
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

        Assertions.assertInstanceOf(SimpleValue.class, lastOperatorExpressionObject.leftValue);
        Assertions.assertInstanceOf(SimpleValue.class, lastOperatorExpressionObject.rightValue);

        Assertions.assertEquals(
                middleValue,
                ((SimpleValue)lastOperatorExpressionObject.leftValue).value.content,
                "invalid expression middle value"
        );

        Assertions.assertEquals(
                lastValue,
                ((SimpleValue)lastOperatorExpressionObject.rightValue).value.content,
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
        final var context = Utils.parseContextOf(expr);
        final var parseResult = NEW_LINES_SENSITIVE.parse(context);

        Assertions.assertTrue(parseResult.isSuccessful(), "the parse result must be successful");

        Assertions.assertDoesNotThrow(
                parseResult::valueOrThrow,
                "the resulted ast object must be non null"
        );

        Assertions.assertInstanceOf(
                BinaryExpressionObject.class,
                parseResult.valueOrThrow(),
                String.format("the resulted ast object must be the binary expression object (`%s`)", expr)
        );

        final var firstOperatorExpressionObject = (BinaryExpressionObject)parseResult.valueOrThrow();

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
        Assertions.assertInstanceOf(SimpleValue.class, firstOperatorExpressionObject.rightValue);

        Assertions.assertEquals(
                lastValue,
                ((SimpleValue)firstOperatorExpressionObject.rightValue).value.content,
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

        Assertions.assertInstanceOf(SimpleValue.class, lastOperatorExpressionObject.leftValue);
        Assertions.assertInstanceOf(SimpleValue.class, lastOperatorExpressionObject.rightValue);

        Assertions.assertEquals(
                firstValue,
                ((SimpleValue)lastOperatorExpressionObject.leftValue).value.content,
                "invalid expression first value"
        );

        Assertions.assertEquals(
                middleValue,
                ((SimpleValue)lastOperatorExpressionObject.rightValue).value.content,
                "invalid expression middle value"
        );
    }

    @Test
    void invalidBinaryExpressionWhereEverythingStartsWithNewLine() {
        final var leftValue = "9.9";
        final var expr = String.format("%s\n*\n0.1\n/\n5\n**\n100", leftValue);
        final var context = Utils.parseContextOf(expr);
        final var parseResult = NEW_LINES_SENSITIVE.parse(context);

        Assertions.assertTrue(parseResult.isSuccessful(), "the parse result must be successful");

        Assertions.assertDoesNotThrow(
                parseResult::valueOrThrow,
                "the resulted ast object must be non null"
        );

        Assertions.assertInstanceOf(
                SimpleValue.class,
                parseResult.valueOrThrow(),
                String.format("the resulted ast object must be the binary expression object (`%s`)", expr)
        );

        final var astObject = (SimpleValue)parseResult.valueOrThrow();

        Assertions.assertEquals(leftValue, astObject.value.content);
        Assertions.assertNotNull(context.current());
        Assertions.assertEquals(Simple.NEWLINE, context.currentOrThrow().type);
    }

    @Test
    void invalidNeverClosedRoundBrackets() {
        final var expr = "(6969";
        final var context = Utils.parseContextOf(expr);
        final var parseResult = NEW_LINES_SENSITIVE.parse(context);

        Assertions.assertTrue(parseResult.isFailed(), "the parse result must be specified as failed");

        Assertions.assertNull(
                parseResult.value(),
                "the resulted ast object must be null"
        );

        Assertions.assertEquals(
                1,
                context.messages.handler.errCount(),
                "there must be exactly one compilation error message"
        );
    }

    @Test
    void invalidEmptyBrackets() {
        final var expr = "()";
        final var context = Utils.parseContextOf(expr);
        final var parseResult = NEW_LINES_SENSITIVE.parse(context);

        Assertions.assertTrue(parseResult.isFailed(), "the parse result must be specified as failed");

        Assertions.assertNull(
                parseResult.value(),
                "the resulted ast object must be null"
        );

        Assertions.assertEquals(
                1,
                context.messages.handler.errCount(),
                "there must be exactly one compilation error message"
        );
    }

    @Test
    void invalidClosingBracketOnOperandPLace() {
        final var expr = "(6969 / )";
        final var context = Utils.parseContextOf(expr);
        final var parseResult = NEW_LINES_SENSITIVE.parse(context);

        Assertions.assertTrue(parseResult.isFailed(), "the parse result must be specified as failed");

        Assertions.assertNull(
                parseResult.value(),
                "the resulted ast object must be null"
        );

        Assertions.assertEquals(
                1,
                context.messages.handler.errCount(),
                "there must be exactly one compilation error message"
        );
    }

    @Test
    void invalidNoOperandForUnaryOperator() {
        final var expr = "not";
        final var context = Utils.parseContextOf(expr);
        final var parseResult = NEW_LINES_SENSITIVE.parse(context);

        Assertions.assertTrue(parseResult.isFailed(), "the parse result must be specified as failed");

        Assertions.assertNull(
                parseResult.value(),
                "the resulted ast object must be null"
        );

        Assertions.assertEquals(
                1,
                context.messages.handler.errCount(),
                "there must be exactly one compilation error message"
        );
    }

    @Test
    void invalidNoSecondOperandForBinaryOperator() {
        final var expr = "6969 /";
        final var context = Utils.parseContextOf(expr);
        final var parseResult = NEW_LINES_SENSITIVE.parse(context);

        Assertions.assertTrue(parseResult.isFailed(), "the parse result must be specified as failed");

        Assertions.assertNull(
                parseResult.value(),
                "the resulted ast object must be null"
        );

        Assertions.assertEquals(
                1,
                context.messages.handler.errCount(),
                "there must be exactly one compilation error message"
        );
    }

    @Test
    void invalidUnaryOperatorOnBinaryOperatorPlace() {
        final var expr = "6969 not";
        final var context = Utils.parseContextOf(expr);
        final var parseResult = NEW_LINES_SENSITIVE.parse(context);

        Assertions.assertTrue(parseResult.isFailed(), "the parse result must be specified as failed");

        Assertions.assertNull(
                parseResult.value(),
                "the resulted ast object must be null"
        );

        Assertions.assertEquals(
                1,
                context.messages.handler.errCount(),
                "there must be exactly one compilation error message"
        );
    }

    @Test
    void invalidBinaryOperatorOnUnaryOperatorPlace() {
        final var expr = "is 6969";
        final var context = Utils.parseContextOf(expr);
        final var parseResult = NEW_LINES_SENSITIVE.parse(context);

        Assertions.assertTrue(parseResult.isFailed(), "the parse result must be specified as failed");

        Assertions.assertNull(
                parseResult.value(),
                "the resulted ast object must be null"
        );

        Assertions.assertEquals(
                1,
                context.messages.handler.errCount(),
                "there must be exactly one compilation error message"
        );
    }

    @Test
    void invalidOpeningBracketOnBinaryOperatorPlace() {
        final var expr = "6969()";
        final var context = Utils.parseContextOf(expr);
        final var parseResult = NEW_LINES_SENSITIVE.parse(context);

        Assertions.assertTrue(parseResult.isFailed(), "the parse result must be specified as failed");

        Assertions.assertNull(
                parseResult.value(),
                "the resulted ast object must be null"
        );

        Assertions.assertEquals(
                1,
                context.messages.handler.errCount(),
                "there must be exactly one compilation error message"
        );
    }
}