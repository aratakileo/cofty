package cofty.v3.core.parser.node;

import cofty.core.parser.ParseContext;
import cofty.type.Representable;
import cofty.v3.core.parser.node.modifier.NodeModifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class EmptyNode implements ParserNode, Representable {
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

    protected boolean postProceed(boolean proceed, @NotNull ParseContext context, @NotNull NodeModifier topLevelModifier) {
        if (proceed) {
            context.next();
            return true;
        }

        if (!topLevelModifier.isGeneral() || topLevelModifier.isPreviewAnchor()) return false;

        if (modifier.isFail())
            context.CRITICAL_MESSAGES.putErr(modifier.failMessageOrThrow());
        else if (modifier.isGeneral())
            context.next();

        return false;
    }

    @Override
    public String toString() {
        return toReprString();
    }
}
