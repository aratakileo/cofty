package cofty.core.semantics.symbol.scope;

import cofty.core.semantics.symbol.*;
import cofty.core.semantics.symbol.path.AbsSymbolPath;
import cofty.core.semantics.symbol.TypeDescriptor;
import org.jetbrains.annotations.NotNull;

public abstract sealed class CompletedFuncScope extends FuncScope<AbsSymbolPath> permits CoftyFuncScope, ExternalFuncScope {
    public CompletedFuncScope(
            @NotNull String name,
            boolean classInitializer,
            @NotNull ArgsSignature<AbsSymbolPath> argsSignature,
            @NotNull TypeDescriptor<AbsSymbolPath> returnedValueTypePath
    ) {
        super(name, classInitializer, argsSignature, returnedValueTypePath);
    }
}
