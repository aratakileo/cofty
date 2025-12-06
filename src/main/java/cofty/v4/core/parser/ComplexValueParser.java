package cofty.v4.core.parser;

import cofty.core.lexer.token.type.Keyword;
import cofty.core.lexer.token.type.Simple;
import cofty.core.lexer.token.type.TokenType;
import cofty.core.lexer.token.type.operator.Bracket;
import cofty.core.lexer.token.type.operator.Separator;
import cofty.util.Lists;
import cofty.v4.core.parser.ast.value.ExpressionValueObject;
import cofty.v4.core.parser.ast.value.complex.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class ComplexValueParser implements Parser<ExpressionValueObject> {
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
    public @NotNull ParseResult<ExpressionValueObject> parse(@NotNull ParseContext context) {
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

        if (segments.size() == 1)
            return ParseResult.successful(segments.getFirst());

        if (segments.isEmpty())
            throw new IllegalStateException();

        return ParseResult.successful(new ComplexValueObject(segments));
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
                ValueExpressionParser.create(false),
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
