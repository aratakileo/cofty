package cofty.core.parser.ast.value;

import cofty.core.lexer.token.TypedToken;
import org.jetbrains.annotations.NotNull;

import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;

public final class BinaryExpressionObject implements ExpressionValueObject {
    public final List<TypedToken<?>> operators;
    public final ExpressionValueObject leftValue, rightValue;

    public BinaryExpressionObject(
            @NotNull List<TypedToken<?>> operators,
            @NotNull ExpressionValueObject leftValue,
            @NotNull ExpressionValueObject rightValue
    ) {
        this.operators = operators;
        this.leftValue = leftValue;
        this.rightValue = rightValue;
    }

    @Override
    public @NotNull List<TypedToken<?>> failTokensRange() {
        return List.of(leftValue.firstFailToken(), rightValue.lastFailToken());
    }

    @Override
    public @NotNull String prettyString(@NotNull String offset, int increase) {
        return MessageFormat.format(
                "{3} binary [{0}; {1}; {2}]",
                operators.stream().map(TypedToken::content).collect(Collectors.joining(" ")),
                leftValue.prettyString("", increase),
                rightValue.prettyString("", increase)
        );
    }
}
