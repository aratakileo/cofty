package cofty.core.parser;

import cofty.Utils;
import cofty.type.Representable;
import cofty.core.parser.ast.value.complex.ComplexValueObject;
import cofty.core.parser.ast.value.complex.FieldAccessObject;
import cofty.core.parser.ast.value.complex.SimpleValueObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class FieldValueAssignmentParserTest {
    @Test
    void validAssigningValueToSimpleDescribedField() {
        final var value = "'ofc it is'";
        final var expr = String.format("validField = %s", value);
        final var context = Utils.parseContextOf(expr);
        final var parseResult = FieldValueAssignmentParser.DEFAULT.parse(context);

        Assertions.assertTrue(parseResult.isSuccessful(), "the parse result must be successful");

        Assertions.assertDoesNotThrow(
                parseResult::valueOrThrow,
                "the resulted ast object must be non null"
        );

        final var astObject = parseResult.valueOrThrow();

        Assertions.assertInstanceOf(FieldAccessObject.class, astObject.field);

        Assertions.assertEquals(
                "validField",
                ((FieldAccessObject)astObject.field).name.content,
                String.format("invalid assignable field name (`%s`)", expr)
        );

        Assertions.assertInstanceOf(SimpleValueObject.class, astObject.value);

        Assertions.assertEquals(
                value,
                ((SimpleValueObject)astObject.value).value.content,
                String.format("invalid assignable value (`%s`)", expr)
        );
    }

    @Test
    void validAssigningValueToComplexDescribedField() {
        final var value = "'ofc it is'";
        final var expr = String.format("'something'!convert.doSomething().validField = %s", value);
        final var context = Utils.parseContextOf(expr);
        final var parseResult = FieldValueAssignmentParser.DEFAULT.parse(context);

        Assertions.assertTrue(parseResult.isSuccessful(), "the parse result must be successful");

        Assertions.assertDoesNotThrow(
                parseResult::valueOrThrow,
                "the resulted ast object must be non null"
        );

        final var astObject = parseResult.valueOrThrow();

        Assertions.assertInstanceOf(ComplexValueObject.class, astObject.field);

        final var fieldObject = (ComplexValueObject)astObject.field;

        Assertions.assertEquals(
                4,
                fieldObject.segments.size(),
                String.format("the complex description of the field must contain four elements (`%s`)", expr)
        );

        Assertions.assertInstanceOf(FieldAccessObject.class, fieldObject.segments.getLast());

        Assertions.assertEquals(
                "validField",
                ((FieldAccessObject)fieldObject.segments.getLast()).name.content,
                String.format("invalid assignable field name (`%s`)", expr)
        );

        Assertions.assertInstanceOf(SimpleValueObject.class, astObject.value);

        Assertions.assertEquals(
                value,
                ((SimpleValueObject)astObject.value).value.content,
                String.format("invalid assignable value (`%s`)", expr)
        );
    }

    @Test
    void validAssigningValueToComplexDescribedFieldWithNewLinesUntilAssignment() {
        final var value = "'ofc it is'";
        final var expr = String.format("'something'\n!\nconvert\n.\ndoSomething()\n.\nvalidField\n= %s", value);
        final var context = Utils.parseContextOf(expr);
        final var parseResult = FieldValueAssignmentParser.DEFAULT.parse(context);

        Assertions.assertTrue(parseResult.isSuccessful(), "the parse result must be successful");

        Assertions.assertDoesNotThrow(
                parseResult::valueOrThrow,
                "the resulted ast object must be non null"
        );

        final var astObject = parseResult.valueOrThrow();

        Assertions.assertInstanceOf(ComplexValueObject.class, astObject.field);

        final var fieldObject = (ComplexValueObject)astObject.field;

        Assertions.assertEquals(
                4,
                fieldObject.segments.size(),
                String.format(
                        "the complex description of the field must contain four elements (`%s`)",
                        Representable.repr(expr, true)
                )
        );

        Assertions.assertInstanceOf(FieldAccessObject.class, fieldObject.segments.getLast());

        Assertions.assertEquals(
                "validField",
                ((FieldAccessObject)fieldObject.segments.getLast()).name.content,
                String.format("invalid assignable field name (`%s`)", Representable.repr(expr, true))
        );

        Assertions.assertInstanceOf(SimpleValueObject.class, astObject.value);

        Assertions.assertEquals(
                value,
                ((SimpleValueObject)astObject.value).value.content,
                String.format("invalid assignable value (`%s`)", Representable.repr(expr, true))
        );
    }

    @Test
    void validParseCancellationCuzNoAssignmentOperatorAfterHypotheticalFieldDescription() {
        final var expr = "hypotheticalField";
        final var context = Utils.parseContextOf(expr);
        final var parseResult = FieldValueAssignmentParser.DEFAULT.parse(context);

        Assertions.assertTrue(parseResult.isCanceled(), "the parse result must be specified as cancelled");

        Assertions.assertNull(
                parseResult.value(),
                "the resulted ast object must be null"
        );
    }

    @Test
    void validParseCancellationCuzExprIsEmpty() {
        final var expr = "";
        final var context = Utils.parseContextOf(expr);
        final var parseResult = FieldValueAssignmentParser.DEFAULT.parse(context);

        Assertions.assertTrue(parseResult.isCanceled(), "the parse result must be specified as cancelled");

        Assertions.assertNull(
                parseResult.value(),
                "the resulted ast object must be null"
        );
    }

    @Test
    void invalidAbsolutelyEverythingStartsWithNewLine() {
        final var value = "'invalid state of the assignable value ;('";
        final var expr = String.format("'something'\n!\nconvert\n.\ndoSomething()\n.\nvalidField\n=\n%s", value);
        final var context = Utils.parseContextOf(expr);
        final var parseResult = FieldValueAssignmentParser.DEFAULT.parse(context);

        Assertions.assertTrue(parseResult.isFailed(), "the parse result must be specified as failed");

        Assertions.assertNull(
                parseResult.value(),
                "the resulted ast object must be null"
        );
    }

    @Test
    void invalidFieldWithNoAssignableValue() {
        final var expr = "invalidField = ";
        final var context = Utils.parseContextOf(expr);
        final var parseResult = FieldValueAssignmentParser.DEFAULT.parse(context);

        Assertions.assertTrue(parseResult.isFailed(), "the parse result must be specified as failed");

        Assertions.assertNull(
                parseResult.value(),
                "the resulted ast object must be null"
        );
    }

    @Test
    void invalidTryAssignValueToBinaryOperatorExpression() {
        final var expr = "a + b = 'it won\\'t work ;('";
        final var context = Utils.parseContextOf(expr);
        final var parseResult = FieldValueAssignmentParser.DEFAULT.parse(context);

        Assertions.assertTrue(parseResult.isFailed(), "the parse result must be specified as failed");

        Assertions.assertNull(
                parseResult.value(),
                "the resulted ast object must be null"
        );
    }

    @Test
    void invalidTryAssignValueToUnaryOperatorExpression() {
        final var expr = "+value = 'it won\\'t work ;('";
        final var context = Utils.parseContextOf(expr);
        final var parseResult = FieldValueAssignmentParser.DEFAULT.parse(context);

        Assertions.assertTrue(parseResult.isFailed(), "the parse result must be specified as failed");

        Assertions.assertNull(
                parseResult.value(),
                "the resulted ast object must be null"
        );
    }

    @Test
    void invalidTryAssignValueToNonField() {
        final var expr = "functionCall() = 'it won\\'t work ;('";
        final var context = Utils.parseContextOf(expr);
        final var parseResult = FieldValueAssignmentParser.DEFAULT.parse(context);

        Assertions.assertTrue(parseResult.isFailed(), "the parse result must be specified as failed");

        Assertions.assertNull(
                parseResult.value(),
                "the resulted ast object must be null"
        );
    }
}