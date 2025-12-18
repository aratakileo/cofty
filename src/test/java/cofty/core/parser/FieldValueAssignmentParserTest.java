package cofty.core.parser;

import cofty.core.compiler.diagnostic.Errors;
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
        final var expr = "validField = %s".formatted(value);
        final var astObject = ParseResultAssert.parse(expr, FieldValueAssignmentParser.DEFAULT)
                .ok()
                .hasNoDiagnosticMessages()
                .value();

        Assertions.assertInstanceOf(FieldAccessObject.class, astObject.fieldView);

        Assertions.assertEquals(
                "validField",
                ((FieldAccessObject)astObject.fieldView).name.content,
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
        final var astObject = ParseResultAssert.parse(expr, FieldValueAssignmentParser.DEFAULT)
                .ok()
                .hasNoDiagnosticMessages()
                .value();

        Assertions.assertInstanceOf(ComplexValueObject.class, astObject.fieldView);

        final var fieldObject = (ComplexValueObject)astObject.fieldView;

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
        final var astObject = ParseResultAssert.parse(expr, FieldValueAssignmentParser.DEFAULT)
                .ok()
                .hasNoDiagnosticMessages()
                .value();

        Assertions.assertInstanceOf(ComplexValueObject.class, astObject.fieldView);

        final var fieldObject = (ComplexValueObject)astObject.fieldView;

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
        ParseResultAssert.parse("hypotheticalField", FieldValueAssignmentParser.DEFAULT)
                .skipped()
                .hasNoDiagnosticMessages();
    }

    @Test
    void validParseCancellationCuzExprIsEmpty() {
        ParseResultAssert.parse("", FieldValueAssignmentParser.DEFAULT)
                .skipped()
                .hasNoDiagnosticMessages();
    }

    @Test
    void invalidAbsolutelyEverythingStartsWithNewLine() {
        final var value = "'invalid state of the assignable value ;('";
        final var expr = String.format("'something'\n!\nconvert\n.\ndoSomething()\n.\nvalidField\n=\n%s", value);

        ParseResultAssert.parse(expr, FieldValueAssignmentParser.DEFAULT)
                .failed()
                .hasErrors(Errors.EXPECTED_ASSIGNABLE_VALUE);
    }

    @Test
    void invalidFieldWithNoAssignableValue() {
        ParseResultAssert.parse("invalidField = ", FieldValueAssignmentParser.DEFAULT)
                .failed()
                .hasErrors(Errors.EXPECTED_ASSIGNABLE_VALUE);
    }

    @Test
    void invalidTryAssignValueToBinaryOperatorExpression() {
        ParseResultAssert.parse("a + b = 'it won\\'t work ;('", FieldValueAssignmentParser.DEFAULT)
                .failed()
                .hasErrors(Errors.DISALLOWED_OPERATOR_BEFORE_ASSIGN);
    }

    @Test
    void invalidTryAssignValueToUnaryOperatorExpression() {
        ParseResultAssert.parse("+value = 'it won\\'t work ;('", FieldValueAssignmentParser.DEFAULT)
                .failed()
                .hasErrors(Errors.DISALLOWED_OPERATOR_BEFORE_ASSIGN);
    }

    @Test
    void invalidTryAssignValueToNonField() {
        ParseResultAssert.parse("functionCall() = 'it won\\'t work ;('", FieldValueAssignmentParser.DEFAULT)
                .failed()
                .hasErrors(Errors.EXPECTED_ASSIGNABLE_FIELD);
    }
}