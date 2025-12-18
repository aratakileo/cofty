package cofty.core.parser;

import cofty.core.compiler.diagnostic.Errors;
import cofty.core.lexer.token.type.Simple;
import cofty.core.lexer.token.type.operator.Bracket;
import cofty.core.parser.ast.*;
import cofty.core.parser.ast.body.BodyObject;
import cofty.core.parser.ast.body.BodyResidentObject;
import cofty.core.parser.ast.body.NotSpecializedBodyObject;
import cofty.core.parser.ast.value.complex.ComplexValueObject;
import cofty.core.parser.ast.value.complex.FuncCallObject;
import cofty.type.Containable;
import cofty.util.Cast;
import cofty.core.compiler.diagnostic.DiagnosticRepresentable;
import cofty.core.parser.ast.value.ReturnStatementObject;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public final class BodyParser implements Parser<BodyObject> {
    public static final BodyParser MODULE_BODY = new BodyParser(
            BodyType.MODULE,
            BodyType.MODULE,
            BodyFormat.NOT_WRAPPED_WITH_CURVES
    );

    private final Set<? extends Parser<? extends BodyResidentObject>> resident_parsers;

    public final BodyType dominantBodyType, actualBodyType;
    public final BodyFormat bodyFormat;

    private BodyParser(
            @NotNull BodyType dominantBodyType,
            @NotNull BodyType actualBodyType,
            @NotNull BodyFormat bodyFormat
    ) {
        this.dominantBodyType = dominantBodyType;
        this.actualBodyType = actualBodyType;
        this.bodyFormat = bodyFormat;

        this.resident_parsers = Set.of(
                actualBodyType == BodyType.CLASS ? FieldDeclarationParser.CLASS_FIELD : FieldDeclarationParser.VARIABLE,
                ReturnStatementParser.DEFAULT,
                FuncDeclarationParser.create(actualBodyType),
                ClassDeclarationParser.create(actualBodyType),
                getNestedBodyParser()
        );
    }

    @Override
    public @NotNull ParseResult<BodyObject> parse(@NotNull ParseContext context) {
        final var startsWithCurve = bodyFormat.mayStartsWithCurveBracket()
                && context.goNextIfCurrentIs(Bracket.CURVE_OPEN);

        if (!startsWithCurve) {
            if (bodyFormat.mustStartsWithCurveBracket()) {
                context.messages.report(Errors.NO_OPENED_BRACKETS, actualBodyType, "curve", "{");
                return ParseResult.failed();
            }

            if (bodyFormat.mayStartsWithCurveBracket())
                return ParseResult.skipped();
        }

        context.goNextIfCurrentIs(Simple.NEWLINE);

        final var bodyFinishedWithReturn = new AtomicBoolean(false);

        final var residentsParseResult = Parser.parseSeparatedQueue(
                context,
                Simple.NEWLINE,
                this::parseLine,
                astObject -> {
                    if (astObject.is(ReturnStatementObject.class))
                        bodyFinishedWithReturn.set(true);

                    if (astObject instanceof BodyObject body && body.finishedWithReturnStatement)
                        bodyFinishedWithReturn.set(true);

                    if (!actualBodyType.applyFilter(astObject))
                        return false;

                    return actualBodyType.isDominant || dominantBodyType.applyFilter(astObject);
                },
                Errors.MISSING_SEPARATOR,
                null,
                "newline",
                "expressions"
        );

        var isFailed = residentsParseResult.isFailed();

        if (context.hasCurrent() && actualBodyType == BodyType.MODULE) {
            context.messages.report(Errors.PARSER_INVALID_SYNTAX);
            return ParseResult.failed();
        }

        if (!isFailed && residentsParseResult.isSkipped() && bodyFormat == BodyFormat.NOT_WRAPPED_WITH_CURVES)
            return ParseResult.skipped();

        if (startsWithCurve && !context.goNextIfCurrentIs(Bracket.CURVE_CLOSE)) {
            context.messages.report(Errors.UNCLOSED_BRACKETS, actualBodyType, "curve", "}");
            return ParseResult.failed();
        }

        if (isFailed) return ParseResult.failed();

        final var residents = residentsParseResult.valueOrDefault(List.of());

        return ParseResult.OK(new NotSpecializedBodyObject(residents, bodyFinishedWithReturn.get()));
    }

    private @NotNull ParseResult<BodyResidentObject> parseLine(@NotNull ParseContext context) {
        final var complexParseResult = parseValueExpressionOrFieldValueAssignment(context);

        if (!complexParseResult.isSkipped()) return Cast.quiet(complexParseResult);

        for (final var parser: resident_parsers) {
            final var parseResult = parser.parse(context);

            if (!parseResult.isSkipped()) return Cast.quiet(parseResult);
        }

        return ParseResult.skipped();
    }

    /*
     *
     * This logic was intentionally removed from the general line parsing cycle in order
     * to avoid double parsing of the value expression, in order to avoid adding the same warnings twice
     * to the list of compilation messages.
     *
     */
    private @NotNull ParseResult<? extends BodyResidentObject> parseValueExpressionOrFieldValueAssignment(
            @NotNull ParseContext context
    ) {
        final var valueExpressionParseResult = ValueExpressionParser.create(true).parse(context);

        if (!valueExpressionParseResult.isOK()) return valueExpressionParseResult;

        final var fieldValueAssignmentParseResult = FieldValueAssignmentParser.DEFAULT.parse(
                context,
                valueExpressionParseResult.valueOrThrow()
        );

        if (!fieldValueAssignmentParseResult.isSkipped()) return fieldValueAssignmentParseResult;

        var parsedExpression = valueExpressionParseResult.valueOrThrow();

        if (parsedExpression instanceof ComplexValueObject complexValueObject)
            parsedExpression = complexValueObject.segments.getLast();

        if (parsedExpression instanceof FuncCallObject) return valueExpressionParseResult;

        context.messages.reportRange(
                valueExpressionParseResult.valueOrThrow().failTokensRange(),
                Errors.EXPRESSION_WITHOUT_EFFECT
        );

        return ParseResult.failed();
    }

    public @NotNull BodyParser getNestedBodyParser() {
        final var newDominantBodyType = dominantBodyType.mergeWith(actualBodyType);

        // helps avoid infinity recursion of this function
        if (newDominantBodyType == dominantBodyType && actualBodyType == BodyType.NESTED)
            return this;

        return new BodyParser(
                newDominantBodyType,
                BodyType.NESTED,
                BodyFormat.NON_STRICT_WRAPPED_WITH_CURVES
        );
    }

    public static @NotNull BodyParser createClassBodyParser(@NotNull BodyType actualBodyType) {
        return new BodyParser(actualBodyType, BodyType.CLASS, BodyFormat.STRICT_WRAPPED_WITH_CURVES);
    }

    public static @NotNull BodyParser createFunctionBodyParser(@NotNull BodyType actualBodyType) {
        return new BodyParser(actualBodyType, BodyType.FUNC, BodyFormat.STRICT_WRAPPED_WITH_CURVES);
    }

    public enum BodyType implements DiagnosticRepresentable, AstObjectFilter<BodyResidentObject> {
        MODULE(true),
        FUNC(true),
        CLASS(true),
        NESTED(false);

        public final boolean isDominant;

        BodyType(boolean isDominant) {
            this.isDominant = isDominant;
        }

        @Override
        public @NotNull String represent() {
            return name().toLowerCase().replaceAll("_+", " ") + " body";
        }

        public @NotNull BodyType mergeWith(@NotNull BodyType newBody) {
            return newBody.isDominant ? newBody : this;
        }

        @Override
        public boolean applyFilter(@NotNull BodyResidentObject astObject) {
            return switch (this) {
                case FUNC, NESTED -> !astObject.isAny(FuncDeclarationObject.class, ClassDeclarationObject.class);
                case CLASS -> astObject.is(DeclarationObject.class);
                case MODULE -> !astObject.is(ReturnStatementObject.class);
            };
        }
    }

    public enum BodyFormat implements Containable<BodyFormat> {
        NOT_WRAPPED_WITH_CURVES,
        /**
         * if there are no curve brackets then the parse result will be specified as failed
         */
        STRICT_WRAPPED_WITH_CURVES,
        /**
         * if there are no curve brackets then the parse result will be specified as skipped
         */
        NON_STRICT_WRAPPED_WITH_CURVES,
        SINGLE_LINE_OR_WRAPPED_WITH_CURVES,
        SINGLE_LINE_ONLY;

        boolean mayStartsWithCurveBracket() {
            return isAny(STRICT_WRAPPED_WITH_CURVES, NON_STRICT_WRAPPED_WITH_CURVES, SINGLE_LINE_OR_WRAPPED_WITH_CURVES);
        }

        boolean mustStartsWithCurveBracket() {
            return this == STRICT_WRAPPED_WITH_CURVES;
        }
    }
}
