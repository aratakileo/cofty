package cofty.core.parser;

import cofty.core.lexer.token.type.operator.Assign;
import cofty.core.parser.ast.FieldValueAssignmentObject;
import cofty.core.parser.ast.value.BinaryExpressionObject;
import cofty.core.parser.ast.value.ExpressionValueObject;
import cofty.core.parser.ast.value.UnaryExpressionObject;
import cofty.core.parser.ast.value.complex.ComplexValueObject;
import cofty.core.parser.ast.value.complex.FieldAccessObject;
import cofty.core.parser.ast.value.complex.ValueSegmentObject;
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
            @Nullable ExpressionValueObject expressionValueObject
    ) {
        context.createIndexSnapshot();

        final var fieldNameParseResult = expressionValueObject == null
                ? ValueExpressionParser.create(true).parse(context) : ParseResult.successful(expressionValueObject);

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

        if (fieldNameParseResult.valueOrThrow() instanceof BinaryExpressionObject binaryExpressionObject) {
            if (binaryExpressionObject.operators.size() == 1) {
                context.messages.addSyntaxErr(
                        String.format(
                                "this operator `%s` is not allowed here before the assignment",
                                binaryExpressionObject.operators.getFirst().content
                        ),
                        binaryExpressionObject.operators.getLast()
                );
                return ParseResult.failed();
            }

            context.messages.addInRangeSyntaxErr(
                    String.format(
                            "this operator `%s %s` is not allowed here before the assignment",
                            binaryExpressionObject.operators.getFirst().content,
                            binaryExpressionObject.operators.getLast().content
                    ),
                    binaryExpressionObject.operators.getFirst(),
                    binaryExpressionObject.operators.getLast()
            );
            return ParseResult.failed();
        }

        if (fieldNameParseResult.valueOrThrow() instanceof UnaryExpressionObject unaryExpressionObject) {
            context.messages.addSyntaxErr(
                    String.format(
                            "this operator `%s` is not allowed here before the assignment",
                            unaryExpressionObject.operator.content
                    ),
                    unaryExpressionObject.operator
            );
            return ParseResult.failed();
        }

        final var lastSegment = fieldNameParseResult.valueOrThrow() instanceof ComplexValueObject complexValue
                ? complexValue.segments.getLast() : (ValueSegmentObject)fieldNameParseResult.valueOrThrow();

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

        final var fieldValueParseResult = ValueExpressionParser.create(true).parse(context);

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
