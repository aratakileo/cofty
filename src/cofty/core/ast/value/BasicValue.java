package cofty.core.ast.value;

import cofty.core.parser.LexemeVisitor;
import cofty.core.parser.ParserContext;
import cofty.core.parser.ProceedResult;
import cofty.core.token.Token;
import cofty.core.token.TokenType;
import cofty.type.Representable;
import org.jetbrains.annotations.NotNull;

public class BasicValue extends Expr<BasicValue> {
    private final String value;
    private final ValueType type;

    public BasicValue(@NotNull String value, @NotNull ValueType type) {
        this.value = value;
        this.type = type;
    }

    @Override
    public String toString() {
        return "BasicValue{" +
                "value=" + Representable.repr(value) +
                ", type=" + type +
                ", computedType=" + Representable.repr(computedType) +
                '}';
    }

    public enum ValueType {
        INT,
        DOUBLE
    }

    public static BasicValue of(@NotNull Token token) {
        final var basicValue = new BasicValue(token.content, switch (token.type) {
            case TokenType.DOUBLE -> ValueType.DOUBLE;
            case TokenType.INT -> ValueType.INT;
            default -> throw new IllegalStateException("Not a basic value token");
        });

        basicValue.setComputedType(basicValue.type.name().toLowerCase());

        return basicValue;
    }

    public static final LexemeVisitor<BasicValue> VISITOR = new LexemeVisitor<>() {
        @Override
        @SuppressWarnings("ConstantConditions")
        public boolean check(@NotNull ParserContext parserContext) {
            return !parserContext.isOutOfBounds() && parserContext.token().type.isIn(TokenType.DOUBLE, TokenType.INT);
        }

        @Override
        @SuppressWarnings("ConstantConditions")
        public @NotNull ProceedResult<BasicValue> consume(@NotNull ParserContext parserContext) {
            if (parserContext.isOutOfBounds())
                throw new IllegalStateException(String.format("`%s` is not consumable", parserContext.token()));

            return ProceedResult.finish(parserContext, of(parserContext.token()));
        }

        @Override
        public @NotNull ProceedResult<BasicValue> fullConsume(@NotNull ParserContext parserContext) {
            final var result = consume(parserContext);
            result.astObject.freeze();
            return result;
        }
    };
}
