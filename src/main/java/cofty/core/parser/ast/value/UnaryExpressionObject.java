package cofty.core.parser.ast.value;

import cofty.core.lexer.token.TypedToken;
import org.jetbrains.annotations.NotNull;

import java.text.MessageFormat;
import java.util.List;

public final class UnaryExpressionObject implements ExpressionValueObject {
    public final TypedToken<?> operator;
    public final ExpressionValueObject value;

    public UnaryExpressionObject(@NotNull TypedToken<?> operator, @NotNull ExpressionValueObject value) {
        this.operator = operator;
        this.value = value;
    }

    @Override
    public @NotNull List<TypedToken<?>> failTokensRange() {
        return List.of(operator, value.lastFailToken());
    }

    @Override
    public @NotNull String prettyString(@NotNull String offset, int increase) {
        return MessageFormat.format(
                "{2} unary [{0}; {1}]",
                operator.content,
                value.prettyString("", increase),
                offset
        );
    }
}
