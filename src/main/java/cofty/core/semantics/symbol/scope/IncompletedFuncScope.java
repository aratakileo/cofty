package cofty.core.semantics.symbol.scope;

import cofty.core.parser.ast.FuncDeclarationObject;
import cofty.core.semantics.symbol.CompletedFieldSymbol;
import cofty.core.semantics.symbol.IncompletedFieldSymbol;
import cofty.core.semantics.symbol.IncompletedSymbol;
import cofty.core.semantics.symbol.TypeDescriptor;
import cofty.core.semantics.symbol.path.RelativeSymbolPath;
import cofty.util.Cast;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public final class IncompletedFuncScope extends FuncScope<RelativeSymbolPath>
        implements IncompletedSymbol<FuncDeclarationObject, CompletedFuncScope> {
    private final FuncDeclarationObject basedOn;

    public IncompletedFuncScope(@NotNull FuncDeclarationObject basedOn) {
        super(
                basedOn.name,
                basedOn.returnType == null ? TypeDescriptor.RELATIVE_NULL : TypeDescriptor.rawReference(basedOn.returnType),
                basedOn.args.stream().map(IncompletedFieldSymbol::create).toList()
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

                if (completionResult.status != CompletionResult.Status.OK)
                    return Cast.quiet(completionResult);

                completedArgs.add(completionResult.completedSymbol);
            }
        }

        final var typeResolveResult = parentOrThrow().resolveTypePath(valueTypeOrThrow().path);

        if (typeResolveResult.isErr())
            return CompletionResult.typeBasedFail(
                    CompletionResult.Status.of(typeResolveResult.unwrapErr()),
                    basedOn.returnType.name
            );

        ((FuncSignaturesScope)parentOrThrow()).remove(this);

        return CompletionResult.OK(new CompletedFuncScope(
                nameToken(),
                TypeDescriptor.reference(typeResolveResult.unwrap()),
                completedArgs
        ));
    }
}
