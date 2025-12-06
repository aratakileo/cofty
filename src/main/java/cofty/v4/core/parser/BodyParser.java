package cofty.v4.core.parser;

import cofty.core.lexer.token.type.Keyword;
import cofty.core.lexer.token.type.Simple;
import cofty.core.lexer.token.type.TokenType;
import cofty.core.lexer.token.type.operator.Bracket;
import cofty.type.Containable;
import cofty.util.Cast;
import cofty.v4.core.parser.ast.*;
import cofty.v4.core.compiler.message.CompilationMessageRepresentable;
import cofty.v4.core.parser.ast.value.ReturnStatementObject;
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

    @Deprecated
    public static final BodyParser FUNC_NESTED_BODY = new BodyParser(
            BodyType.FUNC,
            BodyType.NESTED,
            BodyFormat.NON_STRICT_WRAPPED_WITH_CURVES
    ), NESTED_BODY = new BodyParser(BodyType.MODULE, BodyType.NESTED, BodyFormat.NON_STRICT_WRAPPED_WITH_CURVES),
            FUNC_BODY = new BodyParser(BodyType.MODULE, BodyType.FUNC, BodyFormat.STRICT_WRAPPED_WITH_CURVES);

    @Deprecated
    private final static boolean RUN_LEGACY = false;

    @Deprecated
    private final static List<? extends Parser<? extends BodyResidentObject>> RESIDENT_PARSERS = List.of(
        FieldDeclarationParser.DEFAULT
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
                FieldDeclarationParser.DEFAULT,
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
                context.messages.addSyntaxErr(String.format(
                        "expected the beginning of the %s here using the curly bracket `{`",
                        actualBodyType.represent()
                ));
                return ParseResult.failed();
            }

            if (bodyFormat.mayStartsWithCurveBracket())
                return ParseResult.canceled();
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
                "expected a newline separator here between expressions",
                null
        );

        var isFailed = residentsParseResult.isFailed();

        if (context.hasCurrent() && actualBodyType == BodyType.MODULE) {
            context.messages.addInvalidSyntaxErr();
            return ParseResult.failed();
        }

        if (!isFailed && residentsParseResult.isCanceled() && bodyFormat == BodyFormat.NOT_WRAPPED_WITH_CURVES)
            return ParseResult.canceled();

        if (startsWithCurve && !context.goNextIfCurrentIs(Bracket.CURVE_CLOSE)) {
            context.messages.addSyntaxErr(String.format(
                    "expected the ending of the %s here using the curly bracket `}`",
                    actualBodyType.represent()
            ));
            return ParseResult.failed();
        }

        return isFailed ? ParseResult.failed() : ParseResult.successful(new BodyObject(
                residentsParseResult.valueOrDefault(List.of()),
                bodyFinishedWithReturn.get()
        ));
    }

    @Deprecated
    private @NotNull ParseResult<? extends BodyResidentObject> deprecatedPartOfLineParse(
            @NotNull ParseContext context
    ) {
        final var fnParseResult = checkIfItIsAllowed(
                context,
                Keyword.FUN,
                FuncDeclarationParser.DEFAULT,
                BodyType.MODULE
        );

        if (!fnParseResult.isCanceled()) return Cast.quiet(fnParseResult);

        final var nestedBodyParseResult = (actualBodyType == BodyType.FUNC ? FUNC_NESTED_BODY : NESTED_BODY)
                .parse(context);

        if (!nestedBodyParseResult.isCanceled())
            return Cast.quiet(nestedBodyParseResult);

        final var returnStatementParseResult = checkIfItIsAllowed(
                context,
                Keyword.RETURN,
                ReturnStatementParser.DEFAULT,
                BodyType.FUNC
        );

        if (!returnStatementParseResult.isCanceled())
            return Cast.quiet(returnStatementParseResult);

        for (final var parser: RESIDENT_PARSERS) {
            final var parseResult = parser.parse(context);

            if (!parseResult.isCanceled()) return Cast.quiet(parseResult);
        }

        return ParseResult.canceled();
    }

    private @NotNull ParseResult<BodyResidentObject> parseLine(@NotNull ParseContext context) {
        final var complexParseResult = parseValueExpressionOrFieldValueAssignment(context);

        if (!complexParseResult.isCanceled()) return Cast.quiet(complexParseResult);

        if (RUN_LEGACY) return Cast.quiet(deprecatedPartOfLineParse(context));

        for (final var parser: resident_parsers) {
            final var parseResult = parser.parse(context);

            if (!parseResult.isCanceled()) return Cast.quiet(parseResult);
        }

        return ParseResult.canceled();
    }

    private @NotNull ParseResult<? extends BodyResidentObject> checkIfItIsAllowed(
            @NotNull ParseContext context,
            @NotNull TokenType startsWith,
            @NotNull Parser<? extends BodyResidentObject> parser,
            @NotNull BodyType... allowedBodyTypes
    ) {
        final var token = context.current(startsWith);
        final var parseResult = parser.parse(context);

        var isAllowed = false;

        for (var allowedBodyType: allowedBodyTypes)
            if (actualBodyType == allowedBodyType) {
                isAllowed = true;
                break;
            }

        if (!isAllowed && token != null && token.type.equals(startsWith)) {
            context.messages.addSyntaxErr("not allowed here", token);
            return ParseResult.failed();
        }

        return parseResult;
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

        if (valueExpressionParseResult.isFailed()) return ParseResult.failed();

        if (valueExpressionParseResult.isSuccessful()) {
            final var fieldValueAssignmentParseResult = FieldValueAssignmentParser.DEFAULT.parse(
                    context,
                    valueExpressionParseResult.valueOrThrow()
            );

            if (!fieldValueAssignmentParseResult.isCanceled()) return fieldValueAssignmentParseResult;

            return valueExpressionParseResult;
        }

        return valueExpressionParseResult;
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

    public enum BodyType implements CompilationMessageRepresentable, AstObjectFilter<BodyResidentObject> {
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
         * if there are no curve brackets then the parse result will be specified as canceled
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
