package cofty.core.semantics.symbol;

import org.jetbrains.annotations.NotNull;

public interface WithValueType<T> {
    @NotNull T valueType();
}
