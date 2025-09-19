package cofty.v3.core.parser.node.modifier;

import cofty.type.Representable;
import org.jetbrains.annotations.NotNull;

public class DependedModifier extends ContainerModifier implements Representable {

    public DependedModifier(@NotNull NodeModifier rootModifier) {
        super(rootModifier, ModifierType.DEPENDED, rootModifier.isPrevNodeDepended()
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
                "%s.depended(%s)",
                NodeModifier.class.getSimpleName(),
                Representable.repr(rootModifier)
        );
    }
}
