package cofty.v4.core.parser;

import cofty.core.lexer.token.type.operator.Assign;
import cofty.core.parser.ParseContext;
import cofty.v4.core.parser.ast.FieldValueAssignmentObject;
import cofty.v4.core.parser.ast.value.complex.FieldAccessObject;
import org.jetbrains.annotations.NotNull;

public final class FieldValueAssignmentParser implements Parser<FieldValueAssignmentObject> {
    public static final FieldValueAssignmentParser DEFAULT = new FieldValueAssignmentParser();

    private FieldValueAssignmentParser() {}

    @Override
    public @NotNull ParseResult<FieldValueAssignmentObject> parse(@NotNull ParseContext context) {
        context.createIndexSnapshot();

        final var fieldNameParseResult = ComplexValueParser.DEFAULT.parse(context);

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

        if (!(lastSegment instanceof FieldAccessObject)) {
            context.CRITICAL_MESSAGES.putSyntaxErr(
                    String.format(
                            "expected exactly a field to assign a new value, not the %s",
                            lastSegment.represent()
                    ),
                    lastSegment.failAnchor()
            );
            return ParseResult.failed();
        }

        context.removeIndexSnapshot();

        final var fieldValueParseResult = ValueExpressionParser.DEFAULT.parse(context);

        if (fieldValueParseResult.isFailed()) return ParseResult.failed();

        if (fieldValueParseResult.isCanceled()) {
            context.CRITICAL_MESSAGES.putSyntaxErr("expected the field value");
            return ParseResult.failed();
        }

        return ParseResult.successful(new FieldValueAssignmentObject(
                fieldNameParseResult.valueOrThrow(),
                fieldValueParseResult.valueOrThrow()
        ));
    }
}
