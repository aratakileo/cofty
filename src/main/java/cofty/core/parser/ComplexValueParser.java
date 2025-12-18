package cofty.core.parser;

import cofty.core.compiler.diagnostic.Errors;
import cofty.core.compiler.diagnostic.Warnings;
import cofty.core.lexer.token.type.Keyword;
import cofty.core.lexer.token.type.Simple;
import cofty.core.lexer.token.type.TokenType;
import cofty.core.lexer.token.type.operator.Bracket;
import cofty.core.lexer.token.type.operator.Separator;
import cofty.core.parser.ast.value.complex.*;
import cofty.util.Lists;
import cofty.core.parser.ast.value.ExpressionValueObject;
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
            return ParseResult.skipped();

        context.startSkippingNewLines();

        final var firstSegmentParseResult = context.currentIs(Simple.WORD)
                ? parseFuncCallOrFieldAccess(context, false)
                : new SimpleValueObject(context.advanceOrThrow());

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
                context.messages.report(Errors.EXPECTED_MEMBER_ACCESS);
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
            return ParseResult.OK(segments.getFirst());

        if (segments.isEmpty())
            throw new IllegalStateException();

        return ParseResult.OK(new ComplexValueObject(segments));
    }

    private @Nullable ValueSegmentObject parseFuncCallOrFieldAccess(
            @NotNull ParseContext context,
            boolean isPostfixFuncCall
    ) {
        final var name = context.advanceOrThrow().<Simple>strictAs();

        if (!context.currentIs(Bracket.ROUND_OPEN))
            return isPostfixFuncCall ? FuncCallObject.createPostfixFuncCall(name) : new FieldAccessObject(name);

        final var roundOpeningBracket = context.advanceOrThrow();

        final var argsParseResult = Parser.parseSeparatedQueue(
                context,
                Separator.COMMA,
                ValueExpressionParser.create(false),
                null,
                Errors.MISSING_SEPARATOR,
                Errors.UNEXPECTED_SEPARATOR,
                "comma",
                "arguments",
                "an argument",
                "comma"
        );

        if (argsParseResult.isFailed()) return null;

        if (!context.currentIs(Bracket.ROUND_CLOSE)) {
            context.messages.report(
                    Errors.UNCLOSED_BRACKETS,
                    "arguments description of the function call",
                    "round",
                    ")"
            );

            context.goNext();
            return null;
        }

        final var roundClosingBracket = context.advanceOrThrow();

        if (isPostfixFuncCall && argsParseResult.isSkipped())
            context.messages.reportRange(
                    roundOpeningBracket,
                    roundClosingBracket,
                    Warnings.POSTFIX_FUNC_CALL_WITH_NO_ARGS
            );

        final var args = argsParseResult.valueOrDefault(List.of());

        return new FuncCallObject(name, args, roundClosingBracket, isPostfixFuncCall);
    }

    private int checkPostfixFunctionCalls(
            @NotNull ParseContext context,
            @NotNull ArrayList<ValueSegmentObject> segments,
            boolean isPostfixFuncCall,
            int postfixFunctionCallsCounter
    ) {
        if (!isPostfixFuncCall) {
            if (postfixFunctionCallsCounter > 3) context.messages.reportRange(
                    segments.get(segments.size() - postfixFunctionCallsCounter).firstFailToken(),
                    segments.getLast().lastFailToken(),
                    Warnings.LONG_POSTFIX_CHAIN
            );

            postfixFunctionCallsCounter = 0;
        } else postfixFunctionCallsCounter++;

        return postfixFunctionCallsCounter;
    }
}
