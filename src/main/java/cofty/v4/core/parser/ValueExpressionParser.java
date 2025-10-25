package cofty.v4.core.parser;

import cofty.core.parser.ParseContext;
import cofty.v4.core.parser.ast.value.ValueExpressionObject;
import org.jetbrains.annotations.NotNull;

public final class ValueExpressionParser implements Parser<ValueExpressionObject> {
    public static final ValueExpressionParser DEFAULT = new ValueExpressionParser();

    private ValueExpressionParser() {}

    @Override
    public @NotNull ParseResult<ValueExpressionObject> parse(@NotNull ParseContext context) {
        context.stopSkippingNewLines();

        final var complexValueParseResult = ComplexValueParser.DEFAULT.parse(context);

        if (complexValueParseResult.isCanceled()) {
            context.rollbackSkippingNewLinesState();
            return ParseResult.canceled();
        }

        if (complexValueParseResult.isFailed()) {
            return ParseResult.failed();
        }

        context.rollbackSkippingNewLinesState();
        return ParseResult.successful(new ValueExpressionObject(complexValueParseResult.valueOrThrow()));
    }
}
