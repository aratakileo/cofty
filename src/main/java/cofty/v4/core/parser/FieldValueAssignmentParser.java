package cofty.v4.core.parser;

import cofty.core.lexer.token.type.operator.Assign;
import cofty.v4.core.parser.ast.FieldValueAssignmentObject;
import cofty.v4.core.parser.ast.value.complex.ComplexValueObject;
import cofty.v4.core.parser.ast.value.complex.FieldAccessObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class FieldValueAssignmentParser implements Parser<FieldValueAssignmentObject> {
    public static final FieldValueAssignmentParser DEFAULT = new FieldValueAssignmentParser();

    private FieldValueAssignmentParser() {}

    @Override
    public @NotNull ParseResult<FieldValueAssignmentObject> parse(@NotNull ParseContext context) {
        return parse(context, null);
    }

    /*
     *
     * Such parse logic implementation is not recommended. All necessary values should be passed
     * through the parser class constructor as class fields, and not as arguments to the parsing function,
     * because this violates the principle of universality of the parser interface through one universal parse function.
     * However, in this particular case, such an implementation is acceptable, since it is an addition
     * to optimizing processing, and does not distort the basic logic of this type of parser
     *
     */
    public @NotNull ParseResult<FieldValueAssignmentObject> parse(
            @NotNull ParseContext context,
            @Nullable ComplexValueObject complexValueObject
    ) {
        context.createIndexSnapshot();

        final var fieldNameParseResult = complexValueObject == null
                ? ComplexValueParser.DEFAULT.parse(context) : ParseResult.successful(complexValueObject);

        if (fieldNameParseResult.isCanceled()) {
            context.rollbackIndex();
            return ParseResult.canceled();
        }

        if (fieldNameParseResult.isFailed()) {
            context.removeIndexSnapshot();
            return ParseResult.failed();
        }

        if (!context.goNextIfCurrentIs(Assign.ASSIGN)) {
            context.rollbackIndex();
            return ParseResult.canceled();
        }

        final var lastSegment = fieldNameParseResult.valueOrThrow().segments.getLast();

        context.removeIndexSnapshot();

        if (!(lastSegment instanceof FieldAccessObject)) {
            context.messages.addSyntaxErr(
                    String.format(
                            "expected exactly a field here to assign a new value, not the %s",
                            lastSegment.represent()
                    ),
                    lastSegment.failAnchor()
            );

            return ParseResult.failed();
        }

        final var fieldValueParseResult = ValueExpressionParser.NEWLINES_SENSITIVE.parse(context);

        if (fieldValueParseResult.isFailed()) return ParseResult.failed();

        if (fieldValueParseResult.isCanceled()) {
            context.messages.addSyntaxErr("expected the field value here");
            return ParseResult.failed();
        }

        return ParseResult.successful(new FieldValueAssignmentObject(
                fieldNameParseResult.valueOrThrow(),
                fieldValueParseResult.valueOrThrow()
        ));
    }
}
