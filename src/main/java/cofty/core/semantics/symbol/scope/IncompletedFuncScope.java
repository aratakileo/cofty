package cofty.core.semantics.symbol.scope;

import cofty.core.parser.ast.FuncDeclarationObject;
import cofty.core.parser.ast.TypeDescriptionObject;
import cofty.core.semantics.symbol.CompletedFieldSymbol;
import cofty.core.semantics.symbol.IncompletedFieldSymbol;
import cofty.core.semantics.symbol.IncompletedSymbol;
import cofty.core.semantics.symbol.path.RelativeSymbolPath;
import cofty.core.semantics.symbol.path.SymbolPath;
import cofty.util.Cast;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class IncompletedFuncScope extends FuncScope<TypeDescriptionObject>
        implements IncompletedSymbol<FuncDeclarationObject, CompletedFuncScope> {
    private final FuncDeclarationObject basedOn;

    public IncompletedFuncScope(@NotNull FuncDeclarationObject basedOn) {
        super(
                basedOn.name,
                basedOn.returnType,
                basedOn.args.stream().map(IncompletedFieldSymbol::new).toList()
        );

        this.basedOn = basedOn;
    }

    @Override
    public @NotNull FuncDeclarationObject basedOn() {
        return basedOn;
    }

    @Override
    public @NotNull CompletionResult<CompletedFuncScope> tryComplete() {
        final var argsSnapshot = args.values().stream().toList();
        final var completedArgs = new ArrayList<CompletedFieldSymbol>();

        for (final var arg: argsSnapshot) {
            if (arg instanceof IncompletedFieldSymbol incompletedFieldSymbol) {
                final var completionResult = incompletedFieldSymbol.tryComplete();

                if (completionResult.status != CompletionResult.Status.SUCCESSFUL)
                    return Cast.quiet(completionResult);

                completedArgs.add(completionResult.completedSymbol);
            }
        }

        final var typeResolveResult = parentOrThrow().resolveTypePath(
                valueType() == null
                        ? SymbolPath.relative("null")
                        : SymbolPath.rawTokens(valueType().name)
        );

        if (typeResolveResult.isErr())
            return CompletionResult.typeBasedFail(
                    CompletionResult.Status.of(typeResolveResult.unwrapErr()),
                    valueType().name
            );

        ((FuncSignaturesScope)parentOrThrow()).remove(this);

        return CompletionResult.successful(new CompletedFuncScope(
                nameToken(),
                typeResolveResult.unwrap(),
                completedArgs
        ));
    }

    public boolean canReceive(@NotNull List<RelativeSymbolPath> argSignatures) {
        if (args.size() != argSignatures.size()) return false;

        final var functionArgSymbols = args.values().stream().toList();

        for (var i = 0; i < args.size(); i++)
            if (!SymbolPath.rawTokens(functionArgSymbols.get(i).valueType().name).equals(argSignatures.get(i)))
                return false;

        return true;
    }

    public @NotNull List<RelativeSymbolPath> getArgSignatures() {
        return args.sequencedValues()
                .stream()
                .map(field -> RelativeSymbolPath.rawTokens(field.valueType().name))
                .toList();
    }
}
