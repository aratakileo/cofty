package cofty.core.parser.node;

import cofty.Utils;
import cofty.core.parser.ParseContext;
import cofty.core.parser.ast.AstObject;
import cofty.core.parser.ast.value.PrimitiveValueObject;
import cofty.core.parser.ast.value.expr.ValueExpression;
import cofty.core.parser.ast.value.expr.ValueExpressionObject;
import cofty.core.parser.ast.value.expr.op.BinaryExpressionObject;
import cofty.core.parser.ast.value.expr.op.UnaryExpressionObject;
import cofty.core.parser.node.modifier.NodeModifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

class OperatorExpressionParserTest {
    @Test
    void validBinaryPlusExpression() {
        final var expression = "2 + 3";
        final var context = Utils.parseContextOf(expression);
        final var _result = parseExpression(context);

        Assertions.assertNotNull(
                _result,
                String.format("the result of proceeded `%s` shouldn't be null", expression)
        );

        Assertions.assertEquals(
                0,
                context.messages.count(),
                "there shouldn't be any error or warning messages"
        );

        Assertions.assertInstanceOf(BinaryExpressionObject.class, _result);

        final var result = (BinaryExpressionObject)_result;

        Assertions.assertEquals(
                1,
                result.operator().size(),
                String.format("the proceeded operator of `%s` should consist of one token", expression)
        );

        final var splitted = expression.split(" +");
        final var left = splitted[0];
        final var op = splitted[1];
        final var right = splitted[2];

        Assertions.assertEquals(
                op,
                result.operator().getFirst().content,
                String.format("the proceeded operator of `%s` should be `%s`", expression, op)
        );

        Assertions.assertInstanceOf(
                ValueExpressionObject.class,
                result.leftValue()
        );

        Assertions.assertInstanceOf(
                ValueExpressionObject.class,
                result.rightValue()
        );

        if (((ValueExpressionObject) result.leftValue()).expr() instanceof PrimitiveValueObject rightObject) {
            Assertions.assertEquals(
                    left,
                    Objects.requireNonNull(rightObject.value()).content,
                    String.format("the proceeded left operand of `%s` should be `%s`", expression, left)
            );
        } else throw new IllegalStateException();

        if (((ValueExpressionObject) result.rightValue()).expr() instanceof PrimitiveValueObject leftObject) {
            Assertions.assertEquals(
                    right,
                    Objects.requireNonNull(leftObject.value()).content,
                    String.format("the proceeded right operand of `%s` should be `%s`", expression, right)
            );
        } else throw new IllegalStateException();
    }

    @Test
    void validBinaryIsNotExpression() {
        final var expression = "a is not b";
        final var context = Utils.parseContextOf(expression);
        final var _result = parseExpression(context);

        Assertions.assertNotNull(
                _result,
                String.format("the result of proceeded `%s` shouldn't be null", expression)
        );

        Assertions.assertEquals(
                0,
                context.messages.count(),
                "there shouldn't be any error or warning messages"
        );

        Assertions.assertInstanceOf(BinaryExpressionObject.class, _result);

        final var result = (BinaryExpressionObject)_result;

        Assertions.assertEquals(
                2,
                result.operator().size(),
                String.format("the proceeded operator of `%s` should consist of two tokens", expression)
        );

        final var splitted = expression.split(" +");
        final var left = splitted[0];
        final var opLeft = splitted[1];
        final var opRight = splitted[2];
        final var right = splitted[3];

        Assertions.assertEquals(
                opLeft,
                result.operator().getFirst().content,
                String.format("the proceeded left part of operator of `%s` should be `%s`", expression, opLeft)
        );

        Assertions.assertEquals(
                opRight,
                result.operator().getLast().content,
                String.format("the proceeded right part of operator of `%s` should be `%s`", expression, opRight)
        );

        Assertions.assertInstanceOf(
                ValueExpressionObject.class,
                result.leftValue()
        );

        Assertions.assertInstanceOf(
                ValueExpressionObject.class,
                result.rightValue()
        );

        if (((ValueExpressionObject) result.leftValue()).expr() instanceof PrimitiveValueObject leftObject) {
            Assertions.assertEquals(
                    left,
                    Objects.requireNonNull(leftObject.value()).content,
                    String.format("the proceeded left operand of `%s` should be `%s`", expression, left)
            );
        } else throw new IllegalStateException();

        if (((ValueExpressionObject) result.rightValue()).expr() instanceof PrimitiveValueObject rightObject) {
            Assertions.assertEquals(
                    right,
                    Objects.requireNonNull(rightObject.value()).content,
                    String.format("the proceeded right operand of `%s` should be `%s`", expression, right)
            );
        } else throw new IllegalStateException();
    }

    @Test
    void validBinaryMinusAndUnaryMinusByRightExpression() {
        final var expression = "2 - -3";
        final var context = Utils.parseContextOf(expression);
        final var _result = parseExpression(context);

        Assertions.assertNotNull(
                _result,
                String.format("the result of proceeded `%s` shouldn't be null", expression)
        );

        Assertions.assertEquals(
                0,
                context.messages.count(),
                "there shouldn't be any error or warning messages"
        );

        Assertions.assertInstanceOf(BinaryExpressionObject.class, _result);

        final var result = (BinaryExpressionObject) _result;

        Assertions.assertEquals(
                1,
                result.operator().size(),
                String.format("the proceeded operator of `%s` should consist of one token", expression)
        );

        final var splitted = expression.split(" +");
        final var left = splitted[0];
        final var op = splitted[1];
        final var rightOp = splitted[2].substring(0, 1);
        final var right = splitted[2].substring(1);

        Assertions.assertEquals(
                op,
                result.operator().getFirst().content,
                String.format("the proceeded operator of `%s` should be `%s`", expression, op)
        );

        Assertions.assertInstanceOf(
                ValueExpressionObject.class,
                result.leftValue()
        );

        Assertions.assertInstanceOf(
                UnaryExpressionObject.class,
                result.rightValue()
        );

        if (((ValueExpressionObject) result.leftValue()).expr() instanceof PrimitiveValueObject leftValue) {
            Assertions.assertEquals(
                    left,
                    Objects.requireNonNull(leftValue.value()).content,
                    String.format("the proceeded left operand of `%s` should be `%s`", expression, left)
            );
        } else throw new IllegalStateException();

        final var resultRightExpression = (UnaryExpressionObject)result.rightValue();

        Assertions.assertEquals(
                1,
                resultRightExpression.operator().size(),
                String.format(
                        "the proceeded unary operator of right operand of `%s` should consist of one token",
                        expression
                )
        );

        Assertions.assertEquals(
                rightOp,
                resultRightExpression.operator().getFirst().content,
                String.format("the proceeded unary operator of right operand of `%s` should be `%s`", expression, rightOp)
        );

        Assertions.assertInstanceOf(
                ValueExpressionObject.class,
                resultRightExpression.value()
        );

        if (Objects.requireNonNull((ValueExpressionObject) resultRightExpression.value()).expr() instanceof PrimitiveValueObject rightObject) {
            Assertions.assertEquals(
                    right,
                    rightObject.value().content,
                    String.format("the proceeded right operand value of `%s` should be `%s`", expression, right)
            );
        } else throw new IllegalStateException();
    }

    @Test
    void validBinaryPlusAndBinaryMultiplyExpression() {
        final var expression = "1 + 2 * 3";
        final var context = Utils.parseContextOf(expression);
        final var _result = parseExpression(context);

        Assertions.assertNotNull(
                _result,
                String.format("the result of proceeded `%s` shouldn't be null", expression)
        );

        Assertions.assertEquals(
                0,
                context.messages.count(),
                "there shouldn't be any error or warning messages"
        );

        Assertions.assertInstanceOf(BinaryExpressionObject.class, _result);

        final var result = (BinaryExpressionObject)_result;

        Assertions.assertEquals(
                1,
                result.operator().size(),
                String.format("the proceeded operator of `%s` should consist of one token", expression)
        );

        final var splitted = expression.split(" +");
        final var left = splitted[0];
        final var op = splitted[1];
        final var rightLeft = splitted[2];
        final var rightOp = splitted[3];
        final var rightRight = splitted[4];

        Assertions.assertEquals(
                op,
                result.operator().getFirst().content,
                String.format("the proceeded operator of `%s` should be `%s`", expression, op)
        );

        Assertions.assertInstanceOf(
                ValueExpressionObject.class,
                result.leftValue()
        );

        Assertions.assertInstanceOf(
                BinaryExpressionObject.class,
                result.rightValue()
        );

        final var resultRightExpression = (BinaryExpressionObject)result.rightValue();

        Assertions.assertEquals(
                1,
                resultRightExpression.operator().size(),
                String.format(
                        "the proceeded binary operator of right operand of `%s` should consist of one token",
                        expression
                )
        );

        Assertions.assertEquals(
                rightOp,
                resultRightExpression.operator().getFirst().content,
                String.format(
                        "the proceeded binary operator of right operand of `%s` should be `%s`",
                        expression,
                        rightOp
                )
        );

        Assertions.assertInstanceOf(
                ValueExpressionObject.class,
                resultRightExpression.leftValue()
        );

        Assertions.assertInstanceOf(
                ValueExpressionObject.class,
                resultRightExpression.rightValue()
        );

        if (((ValueExpressionObject) resultRightExpression.leftValue()).expr() instanceof PrimitiveValueObject leftObject) {
            Assertions.assertEquals(
                    rightLeft,
                    Objects.requireNonNull(leftObject.value()).content,
                    String.format("the proceeded middle operand value of `%s` should be `%s`", expression, rightLeft)
            );
        } else throw new IllegalStateException();

        if (Objects.requireNonNull((ValueExpressionObject) resultRightExpression.rightValue()).expr() instanceof PrimitiveValueObject rightObject) {
            Assertions.assertEquals(
                            rightRight,
                    rightObject.value().content,
                    String.format("the proceeded right operand value of `%s` should be `%s`", expression, rightRight)
            );
        } else throw new IllegalStateException();
    }

    private static ValueExpression parseExpression(ParseContext context) {
        final var result = new AtomicReference<AstObject>(null);
        final var parser = new OperatorExpressionParser(
                context,
                NodeModifier.builder().general().astObjectConsumer(result::set).build(), NodeModifier.general(),
                true
        );

        parser.parse();

        return result.get() == null ? null : (ValueExpression)result.get();
    }
}