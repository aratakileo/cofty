package cofty.v3.core.parser.node.modifier;

import cofty.type.Representable;
import org.jetbrains.annotations.NotNull;

public class PreviewAnchorModifier extends ContainerModifier implements Representable {
    public PreviewAnchorModifier(@NotNull NodeModifier rootModifier) {
        super(rootModifier, rootModifier.isPreviewAnchor() || rootModifier.isPeek());
    }

    @Override
    public boolean isPreviewAnchor() {
        return true;
    }

    @Override
    public @NotNull String toReprString() {
        return String.format(
                "%s.previewAnchor(%s)",
                NodeModifier.class.getSimpleName(),
                Representable.repr(rootModifier)
        );
    }
}
