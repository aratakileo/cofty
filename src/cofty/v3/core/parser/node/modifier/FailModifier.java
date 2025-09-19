package cofty.v3.core.parser.node.modifier;

import cofty.type.Representable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FailModifier implements NodeModifier, Representable {
    public final Exception failMessage;

    public FailModifier(@NotNull Exception failMessage) {
        this.failMessage = failMessage;
    }

    @Override
    public boolean isFail() {
        return true;
    }

    @Override
    public @Nullable Exception failMessage() {
        return failMessage;
    }

    @Override
    public @NotNull ModifierType type() {
        return ModifierType.FAIL;
    }

    @Override
    public @NotNull String toReprString() {
        return String.format("%s.fail(%s)", NodeModifier.class.getSimpleName(), Representable.repr(failMessage));
    }
}
