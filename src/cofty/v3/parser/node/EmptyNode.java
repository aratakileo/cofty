package cofty.v3.parser.node;

import cofty.core.parse.ParseContext;
import cofty.v3.parser.node.flag.NodeModifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class EmptyNode implements ParserNode {
    public final NodeModifier modifier;

    private ParserNode next = null;

    public EmptyNode(@NotNull NodeModifier modifier) {
        this.modifier = modifier;
    }

    @Override
    public @Nullable ParserNode next() {
        return next;
    }

    @Override
    public @NotNull NodeModifier modifier() {
        return modifier;
    }

    @Override
    public <E extends ParserNode> @NotNull E then(@NotNull E next) {
        this.next = next;
        return next;
    }

    protected boolean postProceed(boolean proceed, @NotNull ParseContext context, @NotNull NodeModifier topLevelFlag) {
        if (proceed) {
            context.next();
            return true;
        }

        if (!topLevelFlag.isGeneral()) return false;

        if (modifier.isFail())
            context.putErrorMessage(modifier.failMessageOrThrow());
        else if (modifier.isGeneral())
            context.next();

        return false;
    }
}
