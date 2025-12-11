package cofty.core.semantics.symbol.scope;

import cofty.core.semantics.symbol.path.AbsSymbolPath;
import org.jetbrains.annotations.NotNull;

public final class RootScope extends ChildScope {
    public final static String NAME = "#root";
    public final AbsSymbolPath PATH = AbsSymbolPath.ROOT;

    public RootScope() {
        super(NAME);
    }

    @Override
    public void setParent(@NotNull Scope parent) {
        throw new IllegalCallerException();
    }

    @Override
    public @NotNull AbsSymbolPath absPath() {
        return PATH;
    }
}
