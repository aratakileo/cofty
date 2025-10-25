package cofty.v4.core.parser;

import cofty.core.lexer.token.type.Keyword;
import cofty.core.lexer.token.type.Simple;
import cofty.core.lexer.token.type.TokenType;
import cofty.core.lexer.token.type.operator.Bracket;
import cofty.core.lexer.token.type.operator.Separator;
import cofty.core.parser.ParseContext;
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

        if (firstSegmentParseResult == null)
            return ParseResult.failed();

        final var segments = Lists.arrayListOf(firstSegmentParseResult);

        while (context.currentIsAny(SEPARATORS)) {
            final var isPostfixFuncCall = context.currentIs(Separator.EXCLAMATION_MARK);

            context.goNext();

            if (!context.currentIs(Simple.WORD)) {
                context.CRITICAL_MESSAGES.putSyntaxErr("expected a field access or a function call");
                return ParseResult.failed();
            }

            final var segmentParseResult = parseFuncCallOrFieldAccess(context, isPostfixFuncCall);

            if (segmentParseResult == null)
                return ParseResult.failed();

            segments.add(segmentParseResult);
        }

        context.rollbackSkippingNewLinesState();

        return ParseResult.successful(new ComplexValueObject(segments));
    }

    private @Nullable ValueSegmentObject parseFuncCallOrFieldAccess(
            @NotNull ParseContext context,
            boolean isPostfixFuncCall
    ) {
        final var name = context.advanceOrThrow().<Simple>strictAs();

        if (!context.goNextIfCurrentIs(Bracket.ROUND_OPEN))
            return isPostfixFuncCall ? new FuncCallObject(name, List.of(), true) : new FieldAccessObject(name);

        final var args = new ArrayList<ValueExpressionObject>();

        boolean alreadySeparated = true;

        while (!context.goNextIfCurrentIs(Bracket.ROUND_CLOSE)) {
            if (context.currentIs(Separator.COMMA)) {
                if (alreadySeparated) {
                    context.CRITICAL_MESSAGES.putSyntaxErr("expected an argument value, not the comma");
                    return null;
                }

                alreadySeparated = true;
                context.goNext();
                continue;
            }

            final var parseResult = ValueExpressionParser.DEFAULT.parse(context);

            if (!parseResult.isSuccessful() && !context.currentIs(Bracket.ROUND_CLOSE)) {
                context.CRITICAL_MESSAGES.putSyntaxErr("expected the ending of round brackets");
                return null;
            }

            if (!alreadySeparated && !parseResult.isCanceled()) {
                context.CRITICAL_MESSAGES.putSyntaxErr(
                        "expected a comma separator between arguments",
                        ((ComplexValueObject)parseResult.valueOrThrow().expr).segments.getFirst().failAnchor()
                );

                return null;
            }

            if (parseResult.isFailed()) return null;
            if (parseResult.isCanceled()) continue;

            args.add(parseResult.valueOrThrow());
            alreadySeparated = false;
        }

        return new FuncCallObject(name, args.stream().toList(), isPostfixFuncCall);
    }
}
