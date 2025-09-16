package cofty.v3.parser.node.flag;

import cofty.type.Representable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum Flags implements NodeFlag, Representable {
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
    public boolean isFailMessage() {
        return false;
    }

    @Override
    public @Nullable Exception failMessage() {
        return null;
    }

    @Override
    public @NotNull String toReprString() {
        return String.format("%s.%s()", NodeFlag.class.getSimpleName(), name().toLowerCase());
    }
}
