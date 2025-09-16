package cofty.v3.parser.node.flag;

import cofty.type.Representable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum Modifiers implements NodeModifier, Representable {
    GENERAL,
    PEEK;

    @Override
    public boolean isGeneral() {
        return this == GENERAL;
    }

    @Override
    public boolean isPeek() {
        return this == PEEK;
    }

    @Override
    public boolean isFail() {
        return false;
    }

    @Override
    public boolean isSucceed() {
        return false;
    }

    @Override
    public @NotNull String toReprString() {
        return String.format("%s.%s()", NodeModifier.class.getSimpleName(), name().toLowerCase());
    }
}
