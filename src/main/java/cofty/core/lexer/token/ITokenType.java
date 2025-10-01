package cofty.core.lexer.token;

import cofty.core.parser.ParseContext;
import cofty.type.Containable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public interface ITokenType extends Containable<ITokenType> {
    @NotNull TokenType type();
    @Nullable String content();

    default boolean equals(@NotNull ITokenType itype) {
        return itype.type().equals(type())
                && (itype.content() == null) == (content() == null)
                && Objects.equals(itype.content(), content());
    }
}
