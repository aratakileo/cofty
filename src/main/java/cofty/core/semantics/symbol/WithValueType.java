package cofty.core.semantics.symbol;

import cofty.core.semantics.symbol.path.SymbolPath;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public interface WithValueType<T extends SymbolPath<?>> {
    @Nullable TypeDescriptor<T> valueType();

    default @NotNull TypeDescriptor<T> valueTypeOrThrow() {
        return Objects.requireNonNull(valueType());
    }
}
