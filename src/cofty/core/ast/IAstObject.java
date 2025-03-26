package cofty.core.ast;

import org.jetbrains.annotations.NotNull;

public interface IAstObject<T extends IAstObject<?>> {
    boolean frozen();
    void freeze();

    @SuppressWarnings("unchecked")
    default @NotNull T asFrozen() {
        freeze();
        return (T)this;
    }
}
