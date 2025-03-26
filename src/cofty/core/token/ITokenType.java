package cofty.core.token;

import cofty.type.Containable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;

public interface ITokenType extends Containable<ITokenType> {
    @NotNull TokenType type();
    @Nullable String content();

    default boolean equals(@NotNull ITokenType itype) {
        return itype.type().equals(type())
                && (itype.content() == null) == (content() == null)
                && Objects.equals(itype.content(), content());
    }

    @Override
    default boolean isIn(@NotNull ITokenType... values) {
        return Arrays.stream(values).anyMatch(this::equals);
    }
}
