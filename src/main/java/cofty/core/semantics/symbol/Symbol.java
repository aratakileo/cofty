package cofty.core.semantics.symbol;

import cofty.core.semantics.symbol.path.AbsSymbolPath;
import cofty.core.semantics.symbol.scope.Scope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public interface Symbol {
    @NotNull String name();

    @NotNull AbsSymbolPath absPath();

    @Nullable Scope parent();

    default @NotNull Scope parentOrThrow() {
        return Objects.requireNonNull(parent());
    }

    default @NotNull String representedHeader() {
        return String.format("%s [%s]", name(), getClass().getSimpleName());
    }

    default @NotNull String represented() {
        return representedHeader() + ';';
    }
}
