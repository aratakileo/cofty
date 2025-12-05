package cofty.v4.core.parser;

import cofty.v4.core.parser.ast.value.ValueExpressionObject;
import org.jetbrains.annotations.NotNull;

public final class ValueExpressionParser implements Parser<ValueExpressionObject> {
    public final static ValueExpressionParser NEWLINES_SENSITIVE = new ValueExpressionParser(true),
            NEWLINES_INSENSITIVE = new ValueExpressionParser(false);

    public final boolean newlinesSensitive;

    private ValueExpressionParser(boolean newlinesSensitive) {
        this.newlinesSensitive = newlinesSensitive;
    }

    @Override
    public @NotNull ParseResult<ValueExpressionObject> parse(@NotNull ParseContext context) {
        if (newlinesSensitive)
            context.stopSkippingNewLines();
        else context.startSkippingNewLines();

        final var complexValueParseResult = ComplexValueParser.DEFAULT.parse(context);

        if (complexValueParseResult.isCanceled()) {
            context.rollbackSkippingNewLinesState();
            return ParseResult.canceled();
        }

        if (complexValueParseResult.isFailed()) {
            context.rollbackSkippingNewLinesState();
            return ParseResult.failed();
        }

        context.rollbackSkippingNewLinesState();
        return ParseResult.successful(new ValueExpressionObject(complexValueParseResult.valueOrThrow()));
    }

    public static @NotNull ValueExpressionParser create(boolean newlinesSensitive) {
        return newlinesSensitive ? NEWLINES_SENSITIVE : NEWLINES_INSENSITIVE;
    }
}
