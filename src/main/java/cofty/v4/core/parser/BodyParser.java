package cofty.v4.core.parser;

import cofty.core.lexer.token.type.Simple;
import cofty.util.Cast;
import cofty.v4.core.parser.ast.BodyObject;
import cofty.v4.core.parser.ast.BodyResidentObject;
import cofty.v4.core.parser.ast.value.complex.ComplexValueObject;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class BodyParser implements Parser<BodyObject> {
    public static final BodyParser ROOT_BODY = new BodyParser("root");

    private final static List<? extends Parser<BodyResidentObject>> RESIDENT_PARSERS;

    private final String bodyName;

    private BodyParser(String bodyName) {
        this.bodyName = bodyName;
    }

    @Override
    public @NotNull ParseResult<BodyObject> parse(@NotNull ParseContext context) {
        final var residents = new ArrayList<BodyResidentObject>();

        var parseResult = (ParseResult<BodyResidentObject>)null;
        var lineStartsWithToken = context.current();
        var newLineProceeded = true;
        var isFailed = false;

        while (true) {
            parseResult = parseLine(context);

            if (!newLineProceeded && !parseResult.isCanceled()) {
                context.messages.addSyntaxErrBeforeToken(
                        "expected a newline separator between expressions",
                        Objects.requireNonNull(lineStartsWithToken)
                );
                context.goNext();

                isFailed = true;
            }

            if (parseResult.isCanceled()) break;

            if (parseResult.isFailed()) isFailed = true;
            else residents.add(parseResult.valueOrThrow());

            newLineProceeded = context.goNextIfCurrentIs(Simple.NEWLINE);
            lineStartsWithToken = context.current();
        }

        if (context.hasCurrent()) {
            context.messages.addInvalidSyntaxErr();
            return ParseResult.failed();
        }

        if (isFailed) return ParseResult.failed();

        if (residents.isEmpty()) return ParseResult.canceled();

        return ParseResult.successful(new BodyObject(residents));
    }

    private @NotNull ParseResult<BodyResidentObject> parseLine(@NotNull ParseContext context) {
        final var valueExpressionParseResult = ValueExpressionParser.DEFAULT.parse(context);

        if (valueExpressionParseResult.isFailed()) return ParseResult.failed();

        if (valueExpressionParseResult.isSuccessful()) {
            if (valueExpressionParseResult.valueOrThrow().expr instanceof ComplexValueObject complexValueObject) {
                final var fieldValueAssignmentParseResult = FieldValueAssignmentParser.DEFAULT.parse(
                        context,
                        complexValueObject  // to avoid warnings duplication
                );

                if (!fieldValueAssignmentParseResult.isCanceled()) return Cast.unsafe(fieldValueAssignmentParseResult);
            }

            return Cast.unsafe(valueExpressionParseResult);
        }

        for (final var parser: RESIDENT_PARSERS) {
            final var parseResult = parser.parse(context);

            if (!parseResult.isCanceled()) return parseResult;
        }

        return ParseResult.canceled();
    }

    static {
        RESIDENT_PARSERS = Cast.unsafe(List.of(  // the order in which these parsers are called is really important!
                FieldDeclarationParser.DEFAULT
        ));
    }
}
