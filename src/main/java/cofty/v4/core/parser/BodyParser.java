package cofty.v4.core.parser;

import cofty.core.lexer.token.type.Simple;
import cofty.core.lexer.token.type.operator.Bracket;
import cofty.type.Containable;
import cofty.util.Cast;
import cofty.v4.core.parser.ast.BodyObject;
import cofty.v4.core.parser.ast.BodyResidentObject;
import cofty.v4.core.compiler.message.CompilationMessageRepresentable;
import cofty.v4.core.parser.ast.value.complex.ComplexValueObject;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class BodyParser implements Parser<BodyObject> {
    public static final BodyParser ROOT_BODY = new BodyParser(
            BodyType.ROOT,
            BodyFormat.NOT_WRAPPED_WITH_CURVES
    ), NESTED_BODY = new BodyParser(BodyType.NESTED, BodyFormat.NON_STRICT_WRAPPED_WITH_CURVES);

    private final static List<? extends Parser<BodyResidentObject>> RESIDENT_PARSERS;

    private final BodyType bodyType;
    private final BodyFormat bodyFormat;

    private BodyParser(
            @NotNull BodyType bodyType,
            BodyFormat bodyFormat
    ) {
        this.bodyType = bodyType;
        this.bodyFormat = bodyFormat;
    }

    @Override
    public @NotNull ParseResult<BodyObject> parse(@NotNull ParseContext context) {
        final var startsWithCurve = bodyFormat.mayStartsWithCurveBracket() && context.goNextIfCurrentIs(Bracket.CURVE_OPEN);

        if (!startsWithCurve) {
            if (bodyFormat.mustStartsWithCurveBracket()) {
                context.messages.addSyntaxErr(String.format(
                        "expected the beginning of the %s using curly bracket `{`",
                        bodyType.represent()
                ));
                return ParseResult.failed();
            }

            if (bodyFormat.mayStartsWithCurveBracket())
                return ParseResult.canceled();
        }

        final var residents = new ArrayList<BodyResidentObject>();

        var lineParseResult = (ParseResult<BodyResidentObject>)null;
        var lineStartsWithToken = context.current();
        var newLineProceeded = true;
        var isFailed = false;

        while (true) {
            lineParseResult = parseLine(context);

            if (!newLineProceeded && !lineParseResult.isCanceled()) {
                context.messages.addSyntaxErrBeforeToken(
                        "expected a newline separator between expressions",
                        Objects.requireNonNull(lineStartsWithToken)
                );
                context.goNext();

                isFailed = true;
            }

            if (lineParseResult.isCanceled()) break;

            if (lineParseResult.isFailed()) isFailed = true;
            else residents.add(lineParseResult.valueOrThrow());

            newLineProceeded = context.goNextIfCurrentIs(Simple.NEWLINE);
            lineStartsWithToken = context.current();
        }

        if (context.hasCurrent() && bodyType == BodyType.ROOT) {
            context.messages.addInvalidSyntaxErr();
            return ParseResult.failed();
        }

        if (!isFailed && residents.isEmpty() && bodyFormat == BodyFormat.NOT_WRAPPED_WITH_CURVES) return ParseResult.canceled();

        if (startsWithCurve && !context.goNextIfCurrentIs(Bracket.CURVE_CLOSE)) {
            context.messages.addSyntaxErr(String.format(
                    "expected the ending of the %s using curly bracket `}`",
                    bodyType.represent()
            ));
            return ParseResult.failed();
        }

        return isFailed ? ParseResult.failed() : ParseResult.successful(new BodyObject(residents));
    }

    private @NotNull ParseResult<BodyResidentObject> parseLine(@NotNull ParseContext context) {
        final var complexParseResult = parseComplexLine(context);

        if (!complexParseResult.isCanceled()) return complexParseResult;

        for (final var parser: RESIDENT_PARSERS) {
            final var parseResult = parser.parse(context);

            if (!parseResult.isCanceled()) return parseResult;
        }

        return ParseResult.canceled();
    }

    private @NotNull ParseResult<BodyResidentObject> parseComplexLine(
            @NotNull ParseContext context
    ) {
        /*
         *
         * It was intentionally removed from the general line parsing cycle in order
         * to avoid double parsing of the value expression, in order to avoid adding the same warnings twice
         * to the list of compilation messages.
         *
         */

        final var valueExpressionParseResult = ValueExpressionParser.DEFAULT.parse(context);

        if (valueExpressionParseResult.isFailed()) return ParseResult.failed();

        if (valueExpressionParseResult.isSuccessful()) {
            if (valueExpressionParseResult.valueOrThrow().expr instanceof ComplexValueObject complexValueObject) {
                final var fieldValueAssignmentParseResult = FieldValueAssignmentParser.DEFAULT.parse(
                        context,
                        complexValueObject
                );

                if (!fieldValueAssignmentParseResult.isCanceled()) return Cast.unsafe(fieldValueAssignmentParseResult);
            }

            return Cast.unsafe(valueExpressionParseResult);
        }

        return Cast.unsafe(valueExpressionParseResult);
    }

    static {
        RESIDENT_PARSERS = Cast.unsafe(List.of(  // the order in which these parsers are called is very important!
                NESTED_BODY,
                FieldDeclarationParser.DEFAULT
        ));
    }

    public enum BodyType implements CompilationMessageRepresentable {
        ROOT,
        NESTED;

        @Override
        public @NotNull String represent() {
            return name().toLowerCase().replaceAll("_+", " ") + " body";
        }
    }

    public enum BodyFormat implements Containable<BodyFormat> {
        NOT_WRAPPED_WITH_CURVES,
        STRICT_WRAPPED_WITH_CURVES, // `STRICT` means that the body must be wrapped with curve brackets
        NON_STRICT_WRAPPED_WITH_CURVES,
        SINGLE_LINE_OR_WRAPPED_WITH_CURVES,
        SINGLE_LINE_ONLY;

        boolean mayStartsWithCurveBracket() {
            return isIn(STRICT_WRAPPED_WITH_CURVES, NON_STRICT_WRAPPED_WITH_CURVES, SINGLE_LINE_OR_WRAPPED_WITH_CURVES);
        }

        boolean mustStartsWithCurveBracket() {
            return this == STRICT_WRAPPED_WITH_CURVES;
        }

        boolean mustNotStartsWithCurveBracket() {
            return isIn(NOT_WRAPPED_WITH_CURVES, SINGLE_LINE_ONLY);
        }
    }
}
