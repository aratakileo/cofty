package cofty.core.semantics.symbol;

import cofty.core.compiler.diagnostic.DiagnosticRepresentable;
import cofty.core.semantics.symbol.scope.ClassScope;
import cofty.core.semantics.symbol.scope.FuncScope;
import cofty.core.semantics.symbol.scope.FuncSignaturesScope;
import cofty.core.semantics.symbol.scope.ModuleScope;
import org.jetbrains.annotations.NotNull;

public enum SymbolType implements DiagnosticRepresentable {
    FUNCTION,
    CLASS,
    MODULE,
    VARIABLE,
    FIELD,
    ARGUMENT;

    public boolean isinstance(@NotNull Symbol symbol) {
        return switch (this) {
            case VARIABLE, FIELD, ARGUMENT -> symbol instanceof FieldSymbol<?>;
            case CLASS -> symbol instanceof ClassScope;
            case MODULE -> symbol instanceof ModuleScope;
            case FUNCTION -> symbol instanceof FuncSignaturesScope || symbol instanceof FuncScope<?>;
        };
    }

    @Override
    public @NotNull String represent() {
        return name().toLowerCase();
    }

    public static @NotNull SymbolType of(@NotNull Symbol symbol) {
        if (symbol instanceof ClassScope)
            return CLASS;

        if (symbol instanceof FuncScope<?> || symbol instanceof FuncSignaturesScope)
            return FUNCTION;

        if (symbol instanceof FieldSymbol<?>)
            return switch (symbol.parentOrThrow()) {
                case ClassScope _ -> FIELD;
                case FuncScope<?> funcScope -> funcScope.argsSignature.containsName(symbol.name()) ? ARGUMENT : VARIABLE;
                default -> VARIABLE;
            };

        if (symbol instanceof ModuleScope)
            return MODULE;

        throw new IllegalArgumentException();
    }
}
