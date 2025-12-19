package cofty.core.semantics.symbol.scope;

import cofty.core.semantics.symbol.ArgsSignature;
import cofty.core.semantics.symbol.TypeDescriptor;
import cofty.core.semantics.symbol.path.AbsSymbolPath;
import org.jetbrains.annotations.NotNull;

public final class ExternalFuncScope extends CompletedFuncScope {
    public ExternalFuncScope(
            @NotNull String name,
            @NotNull ArgsSignature<AbsSymbolPath> argsSignature,
            @NotNull TypeDescriptor<AbsSymbolPath> returnedValueTypePath
    ) {
        super(name, argsSignature, returnedValueTypePath);
    }
}
