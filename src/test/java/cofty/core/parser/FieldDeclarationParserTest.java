package cofty.core.parser;

import cofty.core.compiler.diagnostic.Errors;
import cofty.core.lexer.token.type.Keyword;
import cofty.core.lexer.token.type.Simple;
import cofty.type.Representable;
import cofty.core.parser.ast.value.complex.SimpleValueObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class FieldDeclarationParserTest {
    @Test
    void validWithSpecifiedTypeAndValue() {
        final var expr = "var mut isValid: bool = true";
        final var astObject = ParseResultAssert.parse(expr, FieldDeclarationParser.VARIABLE)
                .ok()
                .hasNoDiagnosticMessages()
                .value();

        Assertions.assertNotNull(
                astObject.mutable,
                String.format("the mutability indicator of the ast object (`%s`) must be non null", expr)
        );

        Assertions.assertEquals(
                Keyword.MUT,
                astObject.mutable.type,
                String.format("invalid token type of the field mutability indicator (`%s`)", expr)
        );

        Assertions.assertEquals(
                Simple.WORD,
                astObject.name.type,
                String.format("invalid token type of the field name (`%s`)", expr)
        );

        Assertions.assertEquals(
                "isValid",
                astObject.name.content,
                String.format("invalid field name (`%s`)", expr)
        );

        assert astObject.valueType != null;

        Assertions.assertEquals(
                "bool",
                astObject.valueType.name.getFirst().content,
                String.format("invalid field value type (`%s`)", expr)
        );

        assert astObject.value != null;

        Assertions.assertInstanceOf(SimpleValueObject.class, astObject.value);

        final var valueObject = (SimpleValueObject)astObject.value;

        Assertions.assertEquals(
                "true",
                valueObject.value.content,
                String.format("invalid field value (`%s`)", expr)
        );
    }

    @Test
    void validEverythingWithNewLineExceptValue() {
        final var expr = "var\nmut\nisValid\n:\nbool\n= true";
        final var astObject = ParseResultAssert.parse(expr, FieldDeclarationParser.VARIABLE)
                .ok()
                .hasNoDiagnosticMessages()
                .value();

        Assertions.assertNotNull(
                astObject.mutable,
                String.format(
                        "the mutability indicator of the ast object (`%s`) must be non null",
                        Representable.repr(expr, true)
                )
        );

        Assertions.assertEquals(
                Keyword.MUT,
                astObject.mutable.type,
                String.format(
                        "invalid token type of the field mutability indicator (`%s`)",
                        Representable.repr(expr, true)
                )
        );

        Assertions.assertEquals(
                Simple.WORD,
                astObject.name.type,
                String.format(
                        "invalid token type of the field name (`%s`)",
                        Representable.repr(expr, true)
                )
        );

        Assertions.assertEquals(
                "isValid",
                astObject.name.content,
                String.format("invalid field name (`%s`)", Representable.repr(expr, true))
        );

        assert astObject.valueType != null;

        Assertions.assertEquals(
                "bool",
                astObject.valueType.name.getFirst().content,
                String.format("invalid field value type (`%s`)", Representable.repr(expr, true))
        );

        assert astObject.value != null;

        Assertions.assertInstanceOf(SimpleValueObject.class, astObject.value);

        final var valueObject = (SimpleValueObject)astObject.value;

        Assertions.assertEquals(
                "true",
                valueObject.value.content,
                String.format("invalid field value (`%s`)", expr)
        );
    }

    @Test
    void validWithOnlyTypeSpecified() {
        final var expr = "var isValid: bool";
        final var astObject = ParseResultAssert.parse(expr, FieldDeclarationParser.VARIABLE)
                .ok()
                .hasNoDiagnosticMessages()
                .value();

        Assertions.assertEquals(
                Simple.WORD,
                astObject.name.type,
                String.format("invalid token type of the field name (`%s`)", expr)
        );

        Assertions.assertEquals(
                "isValid",
                astObject.name.content,
                String.format("invalid field name (`%s`)", expr)
        );

        assert astObject.valueType != null;

        Assertions.assertEquals(
                "bool",
                astObject.valueType.name.getFirst().content,
                String.format("invalid field value type (`%s`)", expr)
        );
    }

    @Test
    void validWithOnlyValueSpecified() {
        final var expr = "var isValid = true";
        final var astObject = ParseResultAssert.parse(expr, FieldDeclarationParser.VARIABLE)
                .ok()
                .hasNoDiagnosticMessages()
                .value();

        Assertions.assertEquals(
                Simple.WORD,
                astObject.name.type,
                String.format("invalid token type of the field name (`%s`)", expr)
        );

        Assertions.assertEquals(
                "isValid",
                astObject.name.content,
                String.format("invalid field name (`%s`)", expr)
        );

        assert astObject.value != null;

        Assertions.assertInstanceOf(SimpleValueObject.class, astObject.value);

        final var valueObject = (SimpleValueObject)astObject.value;

        Assertions.assertEquals(
                "true",
                valueObject.value.content,
                String.format("invalid field value (`%s`)", expr)
        );
    }

    @Test
    void validParseCancellation() {
        ParseResultAssert.parse("", FieldDeclarationParser.VARIABLE)
                .skipped()
                .hasNoDiagnosticMessages();
    }

    @Test
    void invalidWithNoTypeSpecifiedAfterColonOperator() {
        ParseResultAssert.parse("var isInvalid:", FieldDeclarationParser.VARIABLE)
                .failed()
                .hasErrors(Errors.EXPECTED_FIELD_TYPE);
    }

    @Test
    void invalidWithNoValueSpecifiedAfterAssignOperator() {
        ParseResultAssert.parse("var isInvalid =", FieldDeclarationParser.VARIABLE)
                .failed()
                .hasErrors(Errors.EXPECTED_ASSIGNABLE_VALUE);
    }

    @Test
    void invalidValueStartsWithNewLine() {
        ParseResultAssert.parse("var isInvalid =\ntrue", FieldDeclarationParser.VARIABLE)
                .failed()
                .hasErrors(Errors.EXPECTED_ASSIGNABLE_VALUE);
    }

    @Test
    void invalidWithNoSpecifiedTypeOrValue() {
        ParseResultAssert.parse("var isInvalid", FieldDeclarationParser.VARIABLE)
                .failed()
                .hasErrors(Errors.EXPECTED_ASSIGNMENT_TARGET);
    }
}