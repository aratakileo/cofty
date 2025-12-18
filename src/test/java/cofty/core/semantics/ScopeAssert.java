package cofty.core.semantics;

import cofty.core.semantics.symbol.*;
import cofty.core.semantics.symbol.path.AbsSymbolPath;
import cofty.core.semantics.symbol.path.RelativeSymbolPath;
import cofty.core.semantics.symbol.path.SymbolPath;
import cofty.core.semantics.symbol.scope.ClassScope;
import cofty.core.semantics.symbol.scope.CompletedFuncScope;
import cofty.core.semantics.symbol.scope.FuncSignaturesScope;
import cofty.core.semantics.symbol.scope.Scope;
import cofty.util.Cast;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;

import java.util.Arrays;

public sealed class ScopeAssert<T extends Scope> permits TypedScopeAssert {
    public final T scope;

    public ScopeAssert(@NotNull T scope) {
        this.scope = scope;
    }

    public @NotNull ScopeAssert<T> containsLocalName(@NotNull String name) {
        Assertions.assertTrue(scope.containsLocalName(name));
        return this;
    }

    public @NotNull ScopeAssert<T> containsChildren(int count) {
        Assertions.assertEquals(count, scope.childrenCount());
        return this;
    }

    public <_T extends Symbol> @NotNull _T resolve(@NotNull Class<_T> symbolType, @NotNull String symbolName) {
        final var resolvedSymbol = scope.resolve(symbolName);

        Assertions.assertNotNull(resolvedSymbol);
        Assertions.assertInstanceOf(symbolType, resolvedSymbol);

        return Cast.quiet(resolvedSymbol);
    }

    public <_T extends Scope> @NotNull ScopeAssert<_T> scope(@NotNull Class<_T> symbolType, @NotNull String symbolName) {
        return new ScopeAssert<>(resolve(symbolType, symbolName));
    }

    public @NotNull ScopeAssert<CompletedFuncScope> completedPossibleFuncScope(
            @NotNull String funcName,
            @NotNull String @NotNull... requiredArgTypes
    ) {
        final var funcSignature = ArgsSignature.viewOf(
                Arrays.stream(requiredArgTypes)
                        .map(type -> TypeDescriptor.reference(resolve(ClassScope.class, type).absPath()))
                        .toList()
        );

        return scope(FuncSignaturesScope.class, funcName).scope(CompletedFuncScope.class, funcSignature);
    }

    public @NotNull CompletedFieldSymbolAssert completedField(@NotNull String symbolName) {
        return new CompletedFieldSymbolAssert(resolve(CompletedFieldSymbol.class, symbolName));
    }

    public <_T extends Scope & WithValueType<P>, P extends SymbolPath<P>> @NotNull TypedScopeAssert<_T, P> typedScope(
            @NotNull Class<_T> symbolType,
            @NotNull String symbolName
    ) {
        return new TypedScopeAssert<>(resolve(symbolType, symbolName));
    }

    public <_T extends Symbol & WithValueType<RelativeSymbolPath>> @NotNull ScopeAssert<T> contains(
            @NotNull Class<_T> symbolType,
            @NotNull String symbolName,
            @NotNull String checkableType
    ) {
        resolve(symbolType, symbolName, TypeDescriptor.rawReference(checkableType));
        return this;
    }

    public <_T extends Symbol & WithValueType<RelativeSymbolPath>> @NotNull _T resolve(
            @NotNull Class<_T> symbolType,
            @NotNull String symbolName,
            @NotNull String checkableType
    ) {
        return resolve(symbolType, symbolName, TypeDescriptor.rawReference(checkableType));
    }

    public <_T extends Symbol & WithValueType<AbsSymbolPath>> @NotNull _T resolve(
            @NotNull Class<_T> symbolType,
            @NotNull String symbolName,
            @NotNull AbsSymbolPath checkableType
    ) {
        return resolve(symbolType, symbolName, TypeDescriptor.reference(checkableType));
    }

    public <_T extends Symbol & WithValueType<P>, P extends SymbolPath<?>> @NotNull _T resolve(
            @NotNull Class<_T> symbolType,
            @NotNull String symbolName,
            @NotNull TypeDescriptor<P> checkableType
    ) {
        final var resolvedSymbol = resolve(symbolType, symbolName);

        Assertions.assertEquals(checkableType, resolvedSymbol.valueType());

        return resolvedSymbol;
    }
}
