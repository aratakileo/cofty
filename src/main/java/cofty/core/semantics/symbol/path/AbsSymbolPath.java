package cofty.core.semantics.symbol.path;

import cofty.core.semantics.symbol.scope.RootScope;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class AbsSymbolPath extends SymbolPath<AbsSymbolPath> {
    public static final AbsSymbolPath ROOT = new AbsSymbolPath(List.of(RootScope.NAME));

    AbsSymbolPath(@NotNull List<String> path) {
        super(path);
    }

    AbsSymbolPath(@NotNull String path) {
        super(path);
    }
}
