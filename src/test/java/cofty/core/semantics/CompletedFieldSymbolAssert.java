package cofty.core.semantics;

import cofty.core.semantics.symbol.CompletedFieldSymbol;
import cofty.core.semantics.symbol.TypeDescriptor;
import cofty.core.semantics.symbol.path.AbsSymbolPath;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;

public final class CompletedFieldSymbolAssert {
    public final CompletedFieldSymbol fieldSymbol;

    public CompletedFieldSymbolAssert(@NotNull CompletedFieldSymbol fieldSymbol) {
        this.fieldSymbol = fieldSymbol;
    }

    public @NotNull CompletedFieldSymbolAssert mutable() {
        Assertions.assertTrue(fieldSymbol.isMutable());
        return this;
    }

    public @NotNull CompletedFieldSymbolAssert immutable() {
        Assertions.assertFalse(fieldSymbol.isMutable());
        return this;
    }

    public @NotNull CompletedFieldSymbolAssert valuePassed() {
        Assertions.assertTrue(fieldSymbol.isValuePassed());
        return this;
    }

    public @NotNull CompletedFieldSymbolAssert valueNotPassed() {
        Assertions.assertFalse(fieldSymbol.isValuePassed());
        return this;
    }

    public @NotNull CompletedFieldSymbolAssert checkType(@NotNull TypeDescriptor<AbsSymbolPath> type) {
        Assertions.assertEquals(fieldSymbol.valueTypeOrThrow(), type);
        return this;
    }

    public @NotNull CompletedFieldSymbolAssert checkPossibleType(@NotNull String possibleTypeName) {
        final var classScope = fieldSymbol.parentOrThrow().resolveOrThrow(possibleTypeName);

        Assertions.assertEquals(fieldSymbol.valueTypeOrThrow().path, classScope.absPath());
        return this;
    }
}
