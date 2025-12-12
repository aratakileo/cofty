package cofty.core.parser;

import cofty.core.lexer.token.type.Keyword;
import cofty.core.lexer.token.type.Simple;
import cofty.core.parser.ast.value.ReturnStatementObject;
import org.jetbrains.annotations.NotNull;

public final class ReturnStatementParser implements Parser<ReturnStatementObject> {
    public static final ReturnStatementParser DEFAULT = new ReturnStatementParser();

    private ReturnStatementParser() {}

    @Override
    public @NotNull ParseResult<ReturnStatementObject> parse(@NotNull ParseContext context) {
        if (!context.goNextIfCurrentIs(Keyword.RETURN))
            return ParseResult.skipped();

        if (context.currentIs(Simple.NEWLINE))
            return ParseResult.OK(new ReturnStatementObject(null));

        final var valueParseResult = ValueExpressionParser.create(true).parse(context);

        if (valueParseResult.isSkipped())
            return ParseResult.OK(new ReturnStatementObject(null));

        if (valueParseResult.isFailed())
            return ParseResult.failed();

        return ParseResult.OK(new ReturnStatementObject(valueParseResult.valueOrThrow()));
    }
}
