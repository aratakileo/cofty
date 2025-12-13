package cofty.core.semantics.symbol;

import cofty.core.parser.ast.FieldDeclarationObject;
import cofty.core.parser.ast.value.complex.SimpleValueObject;
import cofty.core.semantics.symbol.path.AbsSymbolPath;
import cofty.core.semantics.symbol.path.RelativeSymbolPath;
import cofty.core.semantics.symbol.scope.Scope;
import cofty.util.Cast;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class IncompletedFieldSymbol
        extends FieldSymbol<RelativeSymbolPath>
        implements IncompletedSymbol<FieldDeclarationObject, CompletedFieldSymbol> {
    private final FieldDeclarationObject basedOn;

    private IncompletedFieldSymbol(
            @NotNull FieldDeclarationObject basedOn,
            @Nullable TypeDescriptor<RelativeSymbolPath> valueTypePath
    ) {
        super(basedOn.nameToken(), valueTypePath, basedOn.mutable != null, basedOn.value != null);
        this.basedOn = basedOn;
    }

    @Override
    public @NotNull FieldDeclarationObject basedOn() {
        return basedOn;
    }

    @Override
    public @NotNull CompletionResult<CompletedFieldSymbol> tryComplete() {
        var fieldTypePath = (AbsSymbolPath)null;

        if (valueType() != null) {
            final var resolveResult = parentOrThrow().resolveTypePath(valueTypeOrThrow().path);

            if (resolveResult.isErr())
                return CompletionResult.typeBasedFail(
                        resolveResult.mapErr(CompletionResult.Status::of).unwrapErr(),
                        basedOn.valueType.name
                );

            fieldTypePath = resolveResult.unwrap();
        }

        if (basedOn.value != null && fieldTypePath == null) {
            var resolveResult = parentOrThrow().resolveTypePath(basedOn.valueOrThrow());

            if (resolveResult.status != Scope.ResolveTypeResult.Status.OK) {
                if (resolveResult.status == Scope.ResolveTypeResult.Status.INCOMPLETE_SYMBOL) {
                    final var completionResult = Objects.requireNonNull(resolveResult.incompletedSymbol).tryComplete();

                    if (completionResult.status != CompletionResult.Status.OK)
                        return Cast.quiet(completionResult);

                    resolveResult = parentOrThrow().resolveTypePath(basedOn.valueOrThrow());

                    if (resolveResult.status != Scope.ResolveTypeResult.Status.OK)
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

        return CompletionResult.OK(new CompletedFieldSymbol(
                nameToken(),
                TypeDescriptor.reference(fieldTypePath),
                isMutable(),
                isValuePassed()
        ));
    }

    public static @NotNull IncompletedFieldSymbol create(@NotNull FieldDeclarationObject basedOn) {
        final var valueType = basedOn.valueType != null ? TypeDescriptor.rawReference(basedOn.valueType) : (
                basedOn.value instanceof SimpleValueObject simpleValueObject
                        ? TypeDescriptor.rawReference(simpleValueObject.valueTypeName()) : null
        );

        return new IncompletedFieldSymbol(basedOn, valueType);
    }
}
