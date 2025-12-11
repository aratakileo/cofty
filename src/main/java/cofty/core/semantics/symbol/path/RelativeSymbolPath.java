package cofty.core.semantics.symbol.path;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class RelativeSymbolPath extends SymbolPath<RelativeSymbolPath> {
    RelativeSymbolPath(@NotNull List<String> path) {
        super(path);
    }
}
