package cofty.core.lexer.token.type.operator;

import cofty.core.lexer.token.type.TokenType;
import org.jetbrains.annotations.NotNull;

public sealed interface Associative extends TokenType permits Binary, Unary {
    @NotNull Associativity associativity();
    int priorityLevel();

    default boolean isRightAssociative() {
        return associativity() == Associativity.RIGHT;
    }

    default boolean isLeftAssociative() {
        return associativity() == Associativity.LEFT;
    }

    static int priorityHigherThan(@NotNull Associative op) {
        return op.priorityLevel() + 1;
    }

    enum Associativity {
        LEFT,
        RIGHT
    }
}
