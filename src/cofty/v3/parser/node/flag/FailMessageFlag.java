package cofty.v3.parser.node.flag;

import cofty.type.Representable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FailMessageFlag implements NodeFlag, Representable {
    public final Exception failMessage;

    public FailMessageFlag(@NotNull Exception failMessage) {
        this.failMessage = failMessage;
    }

    @Override
    public boolean isGeneral() {
        return false;
    }

    @Override
    public boolean isPeek() {
        return false;
    }

    @Override
    public boolean isFailMessage() {
        return true;
    }

    @Override
    public @Nullable Exception failMessage() {
        return failMessage;
    }


    @Override
    public @NotNull String toReprString() {
        return String.format("%s.fail(%s)", NodeFlag.class.getSimpleName(), Representable.repr(failMessage));
    }
}
