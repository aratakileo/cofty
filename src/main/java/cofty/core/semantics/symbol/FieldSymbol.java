package cofty.core.semantics.symbol;

import cofty.core.semantics.symbol.path.SymbolPath;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public sealed abstract class FieldSymbol<T extends SymbolPath<?>> extends ChildSymbol implements WithValueType<T>
        permits IncompletedFieldSymbol, CompletedFieldSymbol {
    private final TypeDescriptor<T> valueTypePath;

    private final boolean isMutable;
    protected boolean isValuePassed;

    protected FieldSymbol(
            @NotNull String name,
            @Nullable TypeDescriptor<T> valueTypePath,
            boolean isMutable,
            boolean isValuePassed
    ) {
        super(name);
        this.valueTypePath = valueTypePath;
        this.isMutable = isMutable;
        this.isValuePassed = isValuePassed;
    }

    @Override
    public @Nullable TypeDescriptor<T> valueType() {
        return valueTypePath;
    }

    @Override
    public @NotNull String represented() {
        return String.format(
                "%s mutable=%s inited=%s -> %s;",
                representedHeader(),
                isMutable,
                isValuePassed,
                valueTypePath
        );
    }

    public boolean isMutable() {
        return isMutable;
    }

    public boolean isValuePassed() {
        return isValuePassed;
    }
}
