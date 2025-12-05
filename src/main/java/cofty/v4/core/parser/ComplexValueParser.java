package cofty.v4.core.parser;

import cofty.core.lexer.token.type.Keyword;
import cofty.core.lexer.token.type.Simple;
import cofty.core.lexer.token.type.TokenType;
import cofty.core.lexer.token.type.operator.Bracket;
import cofty.core.lexer.token.type.operator.Separator;
import cofty.util.Lists;
import cofty.v4.core.parser.ast.value.ValueExpressionObject;
import cofty.v4.core.parser.ast.value.complex.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class ComplexValueParser implements Parser<ComplexValueObject> {
    public static final ComplexValueParser DEFAULT = new ComplexValueParser();

    private static final Set<TokenType> PRIMITIVE_VALUE_TYPES = Set.of(
            Simple.INT,
            Simple.DOUBLE,
            Simple.STR,
            Keyword.TRUE,
            Keyword.FALSE
    );

    private static final Set<TokenType> SEPARATORS = Set.of(Separator.DOT, Separator.EXCLAMATION_MARK);

    private ComplexValueParser() {}

    @Override
    public @NotNull ParseResult<ComplexValueObject> parse(@NotNull ParseContext context) {
        if (!context.currentIs(Simple.WORD) && !context.currentIsAny(PRIMITIVE_VALUE_TYPES))
            return ParseResult.canceled();

        context.startSkippingNewLines();

        final var firstSegmentParseResult = context.currentIs(Simple.WORD)
                ? parseFuncCallOrFieldAccess(context, false) : new SimpleValue(context.advanceOrThrow());

        if (firstSegmentParseResult == null) {
            context.rollbackSkippingNewLinesState();
            return ParseResult.failed();
        }

        final var segments = Lists.arrayListOf(firstSegmentParseResult);

        var postfixFunctionCallsCounter = 0;

        while (context.currentIsAny(SEPARATORS)) {
            final var isPostfixFuncCall = context.currentIs(Separator.EXCLAMATION_MARK);

            postfixFunctionCallsCounter = checkPostfixFunctionCalls(
                    context,
                    segments,
                    isPostfixFuncCall,
                    postfixFunctionCallsCounter
            );

            context.goNext();

            if (!context.currentIs(Simple.WORD)) {
                context.messages.addSyntaxErr("expected a field access or a function call here");
                context.rollbackSkippingNewLinesState();
                context.goNext();

                return ParseResult.failed();
            }

            final var segmentParseResult = parseFuncCallOrFieldAccess(context, isPostfixFuncCall);

            if (segmentParseResult == null) {
                context.rollbackSkippingNewLinesState();
                return ParseResult.failed();
            }

            segments.add(segmentParseResult);
        }

        checkPostfixFunctionCalls(context, segments, false, postfixFunctionCallsCounter);

        context.rollbackSkippingNewLinesState();

        return ParseResult.successful(new ComplexValueObject(segments));
    }

    @Deprecated
    private @Nullable ArrayList<ValueExpressionObject> deprecatedPartOfParseFuncCallOrFieldAccess(
            @NotNull ParseContext context,
            boolean isPostfixFuncCall
    ) {
        final var args = new ArrayList<ValueExpressionObject>();

        var alreadySeparated = true;

        while (!context.currentIs(Bracket.ROUND_CLOSE)) {
            if (context.currentIs(Separator.COMMA)) {
                if (alreadySeparated) {
                    context.messages.addSyntaxErr("expected an argument value here, not the comma");
                    context.goNext();

                    return null;
                }

                alreadySeparated = true;
                context.goNext();
                continue;
            }

            final var parseResult = ValueExpressionParser.NEWLINES_SENSITIVE.parse(context);

            if (!parseResult.isSuccessful() && !context.currentIs(Bracket.ROUND_CLOSE)) {
                context.messages.addSyntaxErr("expected the ending of the round brackets here");
                context.goNext();

                return null;
            }

            if (!alreadySeparated && !parseResult.isCanceled()) {
                context.messages.addSyntaxErrBeforeToken(
                        "expected a comma separator here between arguments",
                        ((ComplexValueObject)parseResult.valueOrThrow().expr).segments.getFirst().failAnchor()
                );

                return null;
            }

            if (parseResult.isFailed()) return null;
            if (parseResult.isCanceled()) continue;

            args.add(parseResult.valueOrThrow());
            alreadySeparated = false;
        }

        return args;
    }

    private @Nullable ValueSegmentObject parseFuncCallOrFieldAccess(
            @NotNull ParseContext context,
            boolean isPostfixFuncCall
    ) {
        final var name = context.advanceOrThrow().<Simple>strictAs();

        if (!context.currentIs(Bracket.ROUND_OPEN))
            return isPostfixFuncCall ? new FuncCallObject(name, List.of(), true) : new FieldAccessObject(name);

        final var roundOpeningBracket = context.advanceOrThrow();

        final var argsParseResult = Parser.parseSeparatedQueue(
                context,
                Separator.COMMA,
                ValueExpressionParser.NEWLINES_INSENSITIVE,
                null,
                "expected a comma separator here between the arguments",
                "expected an argument value here, not the comma"
        );

        if (argsParseResult.isFailed()) return null;

        if (!context.currentIs(Bracket.ROUND_CLOSE)) {
            context.messages.addSyntaxErr("expected the ending of the round brackets here");
            context.goNext();

            return null;
        }

        final var roundClosingBracket = context.advanceOrThrow();

        if (isPostfixFuncCall && argsParseResult.isCanceled())
            context.messages.addInRangeWarn(
                    "a postfix function call without arguments, but with round brackets",
                    roundOpeningBracket,
                    roundClosingBracket
            );

        return new FuncCallObject(name, argsParseResult.valueOrDefault(List.of()), isPostfixFuncCall);
    }

    private int checkPostfixFunctionCalls(
            @NotNull ParseContext context,
            @NotNull ArrayList<ValueSegmentObject> segments,
            boolean isPostfixFuncCall,
            int postfixFunctionCallsCounter
    ) {
        if (!isPostfixFuncCall) {
            if (postfixFunctionCallsCounter > 3) context.messages.addInRangeWarn(
                    "more than three postfix function calls in a row",
                    segments.get(segments.size() - postfixFunctionCallsCounter).failAnchor(),
                    segments.getLast().failAnchor()
            );

            postfixFunctionCallsCounter = 0;
        } else postfixFunctionCallsCounter++;

        return postfixFunctionCallsCounter;
    }
}
