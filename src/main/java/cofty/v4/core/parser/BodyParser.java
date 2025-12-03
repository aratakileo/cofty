package cofty.v4.core.parser;

import cofty.core.lexer.token.type.Keyword;
import cofty.core.lexer.token.type.Simple;
import cofty.core.lexer.token.type.TokenType;
import cofty.core.lexer.token.type.operator.Bracket;
import cofty.type.Containable;
import cofty.v4.core.parser.ast.BodyObject;
import cofty.v4.core.parser.ast.BodyResidentObject;
import cofty.v4.core.compiler.message.CompilationMessageRepresentable;
import cofty.v4.core.parser.ast.value.ReturnStatementObject;
import cofty.v4.core.parser.ast.value.complex.ComplexValueObject;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class BodyParser implements Parser<BodyObject> {
    public static final BodyParser ROOT_BODY = new BodyParser(
            BodyType.ROOT,
            BodyFormat.NOT_WRAPPED_WITH_CURVES
    ), NESTED_BODY = new BodyParser(BodyType.NESTED, BodyFormat.NON_STRICT_WRAPPED_WITH_CURVES),
            FUNC_BODY = new BodyParser(BodyType.FUNC, BodyFormat.STRICT_WRAPPED_WITH_CURVES),
            FUNC_NESTED_BODY = new BodyParser(BodyType.FUNC, BodyFormat.NON_STRICT_WRAPPED_WITH_CURVES);

    private final static List<? extends Parser<? extends BodyResidentObject>> RESIDENT_PARSERS;

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
                        "expected the beginning of the %s here using the curly bracket `{`",
                        bodyType.represent()
                ));
                return ParseResult.failed();
            }

            if (bodyFormat.mayStartsWithCurveBracket())
                return ParseResult.canceled();
        }

        context.goNextIfCurrentIs(Simple.NEWLINE);

        final var residents = new ArrayList<BodyResidentObject>();

        var lineParseResult = (ParseResult<? extends BodyResidentObject>)null;
        var lineStartsWithToken = context.current();
        var newLineProceeded = true;
        var isFailed = false;
        var bodyFinishedWithReturn = false;

        while (true) {
            lineParseResult = parseLine(context);

            if (lineParseResult.isSuccessful()) {
                final var value = lineParseResult.valueOrThrow();

                if (value instanceof ReturnStatementObject)
                    bodyFinishedWithReturn = true;

                if (value instanceof BodyObject body && body.finishedWithReturnStatement)
                    bodyFinishedWithReturn = true;
            }

            if (!newLineProceeded && !lineParseResult.isCanceled()) {
                context.messages.addSyntaxErrBeforeToken(
                        "expected a newline separator here between expressions",
                        Objects.requireNonNull(lineStartsWithToken)
                );

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
                    "expected the ending of the %s here using the curly bracket `}`",
                    bodyType.represent()
            ));
            return ParseResult.failed();
        }

        return isFailed ? ParseResult.failed() : ParseResult.successful(new BodyObject(
                residents,
                bodyFinishedWithReturn
        ));
    }

    private @NotNull ParseResult<? extends BodyResidentObject> parseLine(@NotNull ParseContext context) {
        final var complexParseResult = parseValueExpressionOrFieldValueAssignment(context);

        if (!complexParseResult.isCanceled()) return complexParseResult;

        final var fnParseResult = checkIfItIsAllowed(
                context,
                Keyword.FUN,
                BodyType.ROOT,
                FuncDeclarationParser.DEFAULT
        );

        if (!fnParseResult.isCanceled()) return fnParseResult;

        final var nestedBodyParseResult = (bodyType == BodyType.FUNC ? FUNC_NESTED_BODY : NESTED_BODY).parse(context);

        if (!nestedBodyParseResult.isCanceled())
            return nestedBodyParseResult;

        final var returnStatementParseResult = checkIfItIsAllowed(
                context,
                Keyword.RETURN,
                BodyType.FUNC,
                ReturnStatementParser.DEFAULT
        );

        if (!returnStatementParseResult.isCanceled())
            return returnStatementParseResult;

        for (final var parser: RESIDENT_PARSERS) {
            final var parseResult = parser.parse(context);

            if (!parseResult.isCanceled()) return parseResult;
        }

        return ParseResult.canceled();
    }

    private @NotNull ParseResult<? extends BodyResidentObject> checkIfItIsAllowed(
            @NotNull ParseContext context,
            @NotNull TokenType startsWith,
            @NotNull BodyType allowedBodyType,
            @NotNull Parser<? extends BodyResidentObject> parser
    ) {
        final var token = context.current(startsWith);
        final var parseResult = parser.parse(context);

        if (bodyType != allowedBodyType && token != null && token.type.equals(startsWith)) {
            context.messages.addSyntaxErr("not allowed here", token);
            return ParseResult.failed();
        }

        return parseResult;
    }

    private @NotNull ParseResult<? extends BodyResidentObject> parseValueExpressionOrFieldValueAssignment(
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

                if (!fieldValueAssignmentParseResult.isCanceled()) return fieldValueAssignmentParseResult;
            }

            return valueExpressionParseResult;
        }

        return valueExpressionParseResult;
    }

    static {
        RESIDENT_PARSERS = List.of(  // the order in which these parsers are called is very important!
                FieldDeclarationParser.DEFAULT
        );
    }

    public enum BodyType implements CompilationMessageRepresentable {
        ROOT,
        FUNC,
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
    }
}
