package cofty.v3.core.parser.node.modifier;

import cofty.type.Representable;
import cofty.type.exception.InvalidRootNodeModifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PreviewAnchorModifier implements NodeModifier, Representable {
    public final NodeModifier rootModifier;

    public PreviewAnchorModifier(@NotNull NodeModifier rootModifier) {
        if (rootModifier.isPreviewAnchor())
            throw new InvalidRootNodeModifier(String.format(
                    "`%s` for `%s`",
                    rootModifier.getClass().getSimpleName(),
                    getClass().getSimpleName()
            ));

        this.rootModifier = rootModifier;
    }

    @Override
    public @Nullable ModifierAction action() {
        return rootModifier.action();
    }

    @Override
    public boolean isGeneral() {
        return rootModifier.isGeneral();
    }

    @Override
    public boolean isPeek() {
        return rootModifier.isPeek();
    }

    @Override
    public boolean isFail() {
        return rootModifier.isFail();
    }

    @Override
    public boolean isAction() {
        return rootModifier.isAction();
    }

    @Override
    public boolean isPreviewAnchor() {
        return true;
    }

    @Override
    public @Nullable Exception failMessage() {
        return rootModifier.failMessage();
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
