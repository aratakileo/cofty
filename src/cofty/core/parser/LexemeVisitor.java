package cofty.core.parser;

import cofty.core.ast.IAstObject;
import cofty.core.token.ITokenType;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public interface LexemeVisitor<T extends IAstObject<?>> {
    boolean check(@NotNull ParserContext parserContext);

    @NotNull ProceedResult<T> consume(@NotNull ParserContext parserContext);
    default @NotNull ProceedResult<T> fullConsume(@NotNull ParserContext parserContext) {
        return consume(parserContext).consumeEndOfStatement();
    }

    static <T extends IAstObject<?>> LexemeVisitor<T> byFirst(
            @NotNull ITokenType tokenType,
            @NotNull Function<ParserContext, ProceedResult<T>> parse
    ) {
        return new LexemeVisitor<>() {
            @Override
            @SuppressWarnings("ConstantConditions")
            public boolean check(@NotNull ParserContext parserContext) {
                return !parserContext.isOutOfBounds() && parserContext.token().type.equals(tokenType);
            }

            @Override
            public @NotNull ProceedResult<T> consume(@NotNull ParserContext parserContext) {
                final var result = parse.apply(parserContext);
                result.astObject.freeze();
                return result;
            }
        };
    }
}
