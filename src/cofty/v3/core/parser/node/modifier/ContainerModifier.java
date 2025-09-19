package cofty.v3.core.parser.node.modifier;

import cofty.type.exception.InvalidRootNodeModifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class ContainerModifier implements NodeModifier {
    protected NodeModifier rootModifier;
    public final ModifierType type;

    protected ContainerModifier(
            @NotNull NodeModifier rootModifier,
            @NotNull ModifierType type,
            boolean isInvalidRootModifier
    ) {
        if (isInvalidRootModifier)
            throw new InvalidRootNodeModifier(String.format(
                    "`%s` for `%s`",
                    rootModifier.getClass().getSimpleName(),
                    getClass().getSimpleName()
            ));

        this.rootModifier = rootModifier;
        this.type = type;
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

    @Override
    public @NotNull ModifierType type() {
        return type;
    }

    public @NotNull NodeModifier remove(@NotNull ModifierType type) {
        if (type == type())
            return rootModifier;

        if (rootModifier instanceof ContainerModifier subContainer) {
            rootModifier = subContainer.remove(type);
            return this;
        }

        throw new IllegalStateException();
    }
}
