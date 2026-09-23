package cofty.core.parser.parser;

import cofty.core.lexer.token.type.Keyword;
import cofty.core.parser.ParseContext;
import cofty.core.parser.ParseResult;
import cofty.core.parser.ast.value.ReturnStatementObject;
import org.jetbrains.annotations.NotNull;

public final class ReturnStatementParser implements Parser<ReturnStatementObject> {
    public static final ReturnStatementParser DEFAULT = new ReturnStatementParser();

    private ReturnStatementParser() {}

    @Override
    public @NotNull ParseResult<ReturnStatementObject> parse(@NotNull ParseContext context) {
        final var keywordToken = context.current(Keyword.RETURN);

        if (!context.goNextIfCurrentIs(Keyword.RETURN))
            return ParseResult.skipped();

        final var valueParseResult = ValueExpressionParser.create(true).parse(context);

        if (valueParseResult.isSkipped())
            return ParseResult.OK(new ReturnStatementObject(keywordToken, null));

        if (valueParseResult.isFailed())
            return ParseResult.failed();

        return ParseResult.OK(new ReturnStatementObject(keywordToken, valueParseResult.valueOrThrow()));
    }
}
