package cofty.core.semantics.symbol.scope;

import cofty.core.parser.ast.FieldDeclarationObject;
import cofty.core.parser.ast.FuncDeclarationObject;
import cofty.core.semantics.symbol.CompletedFieldSymbol;
import cofty.core.semantics.symbol.IncompletedFieldSymbol;
import cofty.core.semantics.symbol.IncompletedSymbol;
import cofty.core.semantics.symbol.TypeDescriptor;
import cofty.core.semantics.symbol.path.RelativeSymbolPath;
import cofty.util.Cast;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class IncompletedFuncScope extends FuncScope<RelativeSymbolPath>
        implements IncompletedSymbol<FuncDeclarationObject, CompletedFuncScope> {
    private final FuncDeclarationObject basedOn;

    private IncompletedFuncScope(@NotNull FuncDeclarationObject basedOn) {
        super(
                basedOn.name,
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
        return new IncompletedFuncScope(basedOn);
    }
}
