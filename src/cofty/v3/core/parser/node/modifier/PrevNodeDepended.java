package cofty.v3.core.parser.node.modifier;

import cofty.type.Representable;
import org.jetbrains.annotations.NotNull;

public class PrevNodeDepended extends ContainerModifier implements Representable {

    public PrevNodeDepended(@NotNull NodeModifier rootModifier) {
        super(rootModifier, rootModifier.isPrevNodeDepended()
                || rootModifier.isPeek()
                || rootModifier.isPreviewAnchor()
        );
    }

    @Override
    public boolean isPrevNodeDepended() {
        return true;
    }

    @Override
    public @NotNull String toReprString() {
        return String.format(
                "%s.prevNodeDepended(%s)",
                NodeModifier.class.getSimpleName(),
                Representable.repr(rootModifier)
        );
    }
}
