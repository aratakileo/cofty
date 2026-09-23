package cofty.core.semantics.symbol.scope;

import cofty.core.lexer.token.TypedToken;
import cofty.core.lexer.token.type.Simple;
import cofty.core.semantics.symbol.ArgsSignature;
import cofty.core.semantics.symbol.TypeDescriptor;
import cofty.core.semantics.symbol.WithNameToken;
import cofty.core.semantics.symbol.path.AbsSymbolPath;
import org.jetbrains.annotations.NotNull;

public final class CoftyFuncScope extends CompletedFuncScope implements WithNameToken {
    private final TypedToken<Simple> name;

    public CoftyFuncScope(
            @NotNull TypedToken<Simple> name,
            boolean classInitializer,
            @NotNull ArgsSignature<AbsSymbolPath> argsSignature,
            @NotNull TypeDescriptor<AbsSymbolPath> returnedValueTypePath
    ) {
        super(name.content, classInitializer, argsSignature, returnedValueTypePath);
        this.name = name;
    }

    @Override
    public @NotNull TypedToken<Simple> nameToken() {
        return name;
    }
}
