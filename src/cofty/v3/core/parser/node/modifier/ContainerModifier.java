package cofty.v3.core.parser.node.modifier;

import cofty.type.exception.InvalidRootNodeModifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class ContainerModifier implements NodeModifier {
    public final NodeModifier rootModifier;

    public ContainerModifier(@NotNull NodeModifier rootModifier, boolean isInvalidRootModifier) {
        if (isInvalidRootModifier)
            throw new InvalidRootNodeModifier(String.format(
                    "`%s` for `%s`",
                    rootModifier.getClass().getSimpleName(),
                    getClass().getSimpleName()
            ));

        this.rootModifier = rootModifier;
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
        return rootModifier.isPreviewAnchor();
    }

    @Override
    public boolean isPrevNodeDepended() {
        return rootModifier.isPrevNodeDepended();
    }

    @Override
    public @Nullable ModifierAction action() {
        return rootModifier.action();
    }

    @Override
    public @Nullable Exception failMessage() {
        return rootModifier.failMessage();
    }
}
