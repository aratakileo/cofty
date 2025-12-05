package cofty.v4.core.parser;

import cofty.core.lexer.token.type.Keyword;
import cofty.core.lexer.token.type.Simple;
import cofty.v4.core.parser.ast.value.ReturnStatementObject;
import org.jetbrains.annotations.NotNull;

public final class ReturnStatementParser implements Parser<ReturnStatementObject> {
    public static final ReturnStatementParser DEFAULT = new ReturnStatementParser();

    private ReturnStatementParser() {}

    @Override
    public @NotNull ParseResult<ReturnStatementObject> parse(@NotNull ParseContext context) {
        if (!context.goNextIfCurrentIs(Keyword.RETURN))
            return ParseResult.canceled();

        if (context.currentIs(Simple.NEWLINE))
            return ParseResult.successful(new ReturnStatementObject(null));

        final var valueParseResult = ValueExpressionParser.NEWLINES_SENSITIVE.parse(context);

        if (valueParseResult.isCanceled())
            return ParseResult.successful(new ReturnStatementObject(null));

        if (valueParseResult.isFailed())
            return ParseResult.failed();

        return ParseResult.successful(new ReturnStatementObject(valueParseResult.valueOrThrow()));
    }
}
