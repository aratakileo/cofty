package cofty.v3.core.parser.node.modifier;

import cofty.type.Representable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ActionModifier extends ContainerModifier implements Representable {
    public final ModifierAction action;

    public ActionModifier(@NotNull NodeModifier rootModifier, @NotNull ModifierAction action) {
        super(rootModifier, rootModifier.isAction());

        this.action = action;
    }

    @Override
    public @Nullable ModifierAction action() {
        return action;
    }

    @Override
    public boolean isAction() {
        return true;
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
