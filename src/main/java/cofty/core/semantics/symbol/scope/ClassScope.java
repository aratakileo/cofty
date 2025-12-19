package cofty.core.semantics.symbol.scope;

import org.jetbrains.annotations.NotNull;

public abstract sealed class ClassScope extends ChildScope permits ExternalClassScope, CoftyClassScope {
    public ClassScope(@NotNull String name) {
        super(name);
    }
}
