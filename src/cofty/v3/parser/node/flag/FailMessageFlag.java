package cofty.v3.parser.node.flag;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FailMessageFlag implements NodeFlag {
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
}
