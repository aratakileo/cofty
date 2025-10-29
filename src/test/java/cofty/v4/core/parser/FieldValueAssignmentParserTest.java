package cofty.v4.core.parser;

import cofty.Utils;
import cofty.type.Representable;
import cofty.v4.core.parser.ast.value.complex.ComplexValueObject;
import cofty.v4.core.parser.ast.value.complex.FieldAccessObject;
import cofty.v4.core.parser.ast.value.complex.SimpleValue;
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

        Assertions.assertEquals(
                "validField",
                ((FieldAccessObject)astObject.field.segments.getFirst()).name.content,
                String.format("invalid assignable field name (`%s`)", expr)
        );

        Assertions.assertEquals(
                value,
                ((SimpleValue)((ComplexValueObject)astObject.value.expr).segments.getFirst()).value.content,
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

        Assertions.assertEquals(
                4,
                astObject.field.segments.size(),
                String.format("the complex description of the field must contain four elements (`%s`)", expr)
        );

        Assertions.assertEquals(
                "validField",
                ((FieldAccessObject)astObject.field.segments.getLast()).name.content,
                String.format("invalid assignable field name (`%s`)", expr)
        );

        Assertions.assertEquals(
                value,
                ((SimpleValue)((ComplexValueObject)astObject.value.expr).segments.getFirst()).value.content,
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

        Assertions.assertEquals(
                4,
                astObject.field.segments.size(),
                String.format(
                        "the complex description of the field must contain four elements (`%s`)",
                        Representable.repr(expr, true)
                )
        );

        Assertions.assertEquals(
                "validField",
                ((FieldAccessObject)astObject.field.segments.getLast()).name.content,
                String.format("invalid assignable field name (`%s`)", Representable.repr(expr, true))
        );

        Assertions.assertEquals(
                value,
                ((SimpleValue)((ComplexValueObject)astObject.value.expr).segments.getFirst()).value.content,
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
    void invalidTryAssignValueToNonField() {
        final var expr = "functionCall() = 'it won't work ;('";
        final var context = Utils.parseContextOf(expr);
        final var parseResult = FieldValueAssignmentParser.DEFAULT.parse(context);

        Assertions.assertTrue(parseResult.isFailed(), "the parse result must be specified as failed");

        Assertions.assertNull(
                parseResult.value(),
                "the resulted ast object must be null"
        );
    }
}