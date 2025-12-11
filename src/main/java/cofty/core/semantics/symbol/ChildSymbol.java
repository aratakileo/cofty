package cofty.core.semantics.symbol;

import cofty.core.semantics.symbol.path.AbsSymbolPath;
import cofty.core.semantics.symbol.scope.Scope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class ChildSymbol implements Symbol {
    private final String name;

    private Scope parent = null;
    private AbsSymbolPath absPath;

    protected ChildSymbol(@NotNull String name) {
        this.name = name;
    }

    public void setParent(@NotNull Scope parent) {
        if (this.parent != null)
            throw new IllegalCallerException();

        this.parent = parent;
        this.absPath = parent.absPath().merge(name());
    }

    @Override
    public @NotNull String name() {
        return name;
    }

    @Override
    public @Nullable Scope parent() {
        return parent;
    }

    @Override
    public @NotNull AbsSymbolPath absPath() {
        return absPath;
    }
}
