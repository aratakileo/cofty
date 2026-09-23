package cofty.core.semantics.symbol.scope;

import cofty.core.lexer.token.TypedToken;
import cofty.core.lexer.token.type.Simple;
import cofty.core.parser.ast.ClassDeclarationObject;
import cofty.core.parser.ast.FuncDeclarationObject;
import cofty.core.semantics.symbol.*;
import cofty.core.semantics.symbol.path.RelativeSymbolPath;
import org.jetbrains.annotations.NotNull;

public final class IncompletedFuncScope extends FuncScope<RelativeSymbolPath>
        implements IncompletedSymbol<FuncDeclarationObject, CompletedFuncScope>, WithNameToken {
    private final FuncDeclarationObject basedOn;

    private IncompletedFuncScope(boolean classInitializer, @NotNull FuncDeclarationObject basedOn) {
        super(
                basedOn.name.content,
                classInitializer,
                basedOn.argsSignature(),
                basedOn.returnType == null ? TypeDescriptor.RELATIVE_NULL : TypeDescriptor.rawReference(basedOn.returnType)
        );

        this.basedOn = basedOn;
    }

    @Override
    public @NotNull FuncDeclarationObject basedOn() {
        return basedOn;
    }

    public static @NotNull IncompletedFuncScope create(@NotNull FuncDeclarationObject basedOn) {
        return new IncompletedFuncScope(false, basedOn);
    }

    @Override
    public @NotNull TypedToken<Simple> nameToken() {
        return basedOn.nameToken();
    }
}
