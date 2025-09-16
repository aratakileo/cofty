package cofty.v3.core.parser.node.modifier;

import cofty.type.Representable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ActionModifier implements NodeModifier, Representable {
    public final NodeModifier rootModifier;
    public final ModifierAction action;

    public ActionModifier(@NotNull NodeModifier rootModifier, @NotNull ModifierAction action) {
        this.action = action;

        if (rootModifier.isAction())
            throw new IllegalStateException();

        this.rootModifier = rootModifier;
    }

    @Override
    public @Nullable ModifierAction action() {
        return action;
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
        return true;
    }

    @Override
    public @Nullable Exception failMessage() {
        return rootModifier.failMessage();
    }

    @Override
    public @NotNull String toReprString() {
        return String.format(
                "new %s(%s, %s)",
                this.getClass().getSimpleName(),
                Representable.repr(rootModifier),
                action
        );
    }
}
