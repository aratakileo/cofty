package cofty.core.lexer.token.type;

import cofty.type.Representable;
import org.jetbrains.annotations.NotNull;

public enum Modifier implements TokenType, Representable {
    PUBLIC,
    PRIVATE,
    STATIC;

    public boolean isAccessModifier() {
        return isIn(PUBLIC, PRIVATE);
    }

    @Override
    public @NotNull Simple type() {
        return Simple.KW;
    }

    @Override
    public @NotNull String content() {
        return name().toLowerCase();
    }

    @Override
    public @NotNull String toReprString() {
        return getClass().getSimpleName() + '.' + name();
    }

    public static @NotNull Modifier of(@NotNull String kw) {
        for (final var _kw: values())
            if (_kw.name().toLowerCase().equals(kw))
                return _kw;

        throw new IllegalStateException(String.format("`%s` is not a modifier", kw));
    }

    public static boolean is(@NotNull String kw) {
        return kw.equals("public") || kw.equals("private") || kw.equals("static");
    }
}
