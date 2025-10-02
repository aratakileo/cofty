package cofty.core.lexer.token.type;

import cofty.type.Containable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public interface TokenType extends Containable<TokenType> {
    @NotNull Simple type();
    @Nullable String content();

    default boolean equals(@NotNull TokenType itype) {
        return itype.type().equals(type())
                && (itype.content() == null) == (content() == null)
                && Objects.equals(itype.content(), content());
    }
}
