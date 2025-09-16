package cofty.v3.parser.node.flag;

import cofty.type.Representable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SucceedModifier implements NodeModifier, Representable {
    public final NodeModifier rootModifier;
    public final SucceedApplier succeedApplier;

    public SucceedModifier(@NotNull NodeModifier rootModifier, @NotNull SucceedApplier succeedApplier) {
        this.succeedApplier = succeedApplier;

        if (rootModifier.isSucceed())
            throw new IllegalStateException();

        this.rootModifier = rootModifier;
    }

    @Override
    public @Nullable SucceedApplier succeedApplier() {
        return succeedApplier;
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
    public @Nullable Exception failMessage() {
        return rootModifier.failMessage();
    }

    @Override
    public boolean isSucceed() {
        return true;
    }

    @Override
    public @NotNull String toReprString() {
        return String.format(
                "new %s(%s, %s)",
                this.getClass().getSimpleName(),
                Representable.repr(rootModifier),
                succeedApplier
        );
    }
}
