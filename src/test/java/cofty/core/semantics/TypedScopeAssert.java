package cofty.core.semantics;

import cofty.core.semantics.symbol.TypeDescriptor;
import cofty.core.semantics.symbol.WithValueType;
import cofty.core.semantics.symbol.path.SymbolPath;
import cofty.core.semantics.symbol.scope.Scope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;

public final class TypedScopeAssert<T extends WithValueType<P> & Scope, P extends SymbolPath<P>> extends ScopeAssert<T> {
    public TypedScopeAssert(@NotNull T scope) {
        super(scope);
    }

    public @NotNull TypedScopeAssert<T, P> checkType(@Nullable TypeDescriptor<P> valueType) {
        Assertions.assertEquals(valueType, scope.valueType());
        return this;
    }

    public @NotNull TypedScopeAssert<T, P> checkType(@Nullable String valueType) {
        Assertions.assertEquals(valueType == null ? null : TypeDescriptor.rawReference(valueType), scope.valueType());
        return this;
    }
}
