package cofty.core.semantics.symbol;

import cofty.core.parser.ast.FieldDeclarationObject;
import cofty.core.parser.ast.TypeDescriptionObject;
import cofty.core.semantics.symbol.path.AbsSymbolPath;
import cofty.core.semantics.symbol.scope.Scope;
import cofty.util.Cast;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class IncompletedFieldSymbol
        extends FieldSymbol<TypeDescriptionObject>
        implements IncompletedSymbol<FieldDeclarationObject, CompletedFieldSymbol> {
    private final FieldDeclarationObject basedOn;

    public IncompletedFieldSymbol(@NotNull FieldDeclarationObject basedOn) {
        super(basedOn.name, basedOn.valueType, basedOn.mutable != null, basedOn.value != null);
        this.basedOn = basedOn;
    }

    @Override
    public @NotNull FieldDeclarationObject basedOn() {
        return basedOn;
    }

    @Override
    public @NotNull CompletionResult<CompletedFieldSymbol> tryComplete() {
        var fieldTypePath = (AbsSymbolPath)null;

        if (basedOn.valueType != null) {
            final var resolveResult = parentOrThrow().resolveTypePath(basedOn.valueType);

            if (resolveResult.isErr())
                return CompletionResult.typeBasedFail(
                        resolveResult.mapErr(CompletionResult.Status::of).unwrapErr(),
                        basedOn.valueType.name
                );

            fieldTypePath = resolveResult.unwrap();
        }

        if (fieldTypePath == null || basedOn.value != null) {
            var resolveResult = parentOrThrow().resolveTypePath(basedOn.valueOrThrow());

            if (resolveResult.status != Scope.ResolveTypeResult.Status.SUCCESSFUL) {
                if (resolveResult.status == Scope.ResolveTypeResult.Status.INCOMPLETE_SYMBOL) {
                    final var completionResult = Objects.requireNonNull(resolveResult.incompletedSymbol).tryComplete();

                    if (completionResult.status != CompletionResult.Status.SUCCESSFUL)
                        return Cast.quiet(completionResult);

                    resolveResult = parentOrThrow().resolveTypePath(basedOn.valueOrThrow());

                    if (resolveResult.status != Scope.ResolveTypeResult.Status.SUCCESSFUL)
                        throw new IllegalStateException();

                    if (fieldTypePath == null)
                        fieldTypePath = resolveResult.successfullyResolvedPath;
                } else return CompletionResult.failOf(resolveResult);

                if (!Objects.requireNonNull(fieldTypePath).equals(Objects.requireNonNull(
                        resolveResult.successfullyResolvedPath
                )))
                    return CompletionResult.notSuitableValueType(
                            Cast.quiet(Objects.requireNonNull(basedOn.valueType).name),
                            resolveResult.successfullyResolvedPath
                    );
            } else fieldTypePath = resolveResult.successfullyResolvedPath;
        }

        return CompletionResult.successful(new CompletedFieldSymbol(
                nameToken(),
                Objects.requireNonNull(fieldTypePath),
                isMutable(),
                isValuePassed()
        ));
    }
}
