package cofty.core.semantics.symbol;

import cofty.core.semantics.symbol.path.AbsSymbolPath;
import org.jetbrains.annotations.NotNull;

public final class ExternalFieldSymbol extends CompletedFieldSymbol {
    public ExternalFieldSymbol(
            @NotNull String name,
            @NotNull TypeDescriptor<AbsSymbolPath> valueTypePath,
            boolean isMutable,
            boolean isValuePassed
    ) {
        super(name, valueTypePath, isMutable, isValuePassed);
    }
}
