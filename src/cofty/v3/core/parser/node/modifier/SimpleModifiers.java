package cofty.v3.core.parser.node.modifier;

import cofty.type.Representable;
import org.jetbrains.annotations.NotNull;

public enum SimpleModifiers implements NodeModifier, Representable {
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
    public @NotNull ModifierType type() {
        return this == GENERAL ? ModifierType.GENERAL : ModifierType.PEEK;
    }

    @Override
    public @NotNull String toReprString() {
        return String.format("%s.%s()", NodeModifier.class.getSimpleName(), name().toLowerCase());
    }
}
