package cofty.v3.parser.node.flag;

import org.jetbrains.annotations.Nullable;

public enum Flags implements NodeFlag {
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
}
