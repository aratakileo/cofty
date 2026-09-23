package cofty.core.semantics.symbol.scope;

import cofty.core.compiler.diagnostic.Errors;
import cofty.core.lexer.token.TypedToken;
import cofty.core.lexer.token.type.Simple;
import cofty.core.lexer.token.type.TokenType;
import cofty.core.semantics.symbol.*;
import cofty.core.semantics.symbol.path.AbsSymbolPath;
import cofty.core.semantics.symbol.path.RelativeSymbolPath;
import cofty.core.semantics.symbol.path.SymbolPath;
import cofty.type.Result;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public interface Scope extends Symbol {
    boolean containsLocalName(@NotNull String name);

    @Nullable Symbol resolve(@NotNull String name);

    default @Nullable Symbol resolveLocal(@NotNull String name) {
        return containsLocalName(name) ? resolve(name) : null;
    }

    default @NotNull Symbol resolveLocalOrThrow(@NotNull String name) {
        return Objects.requireNonNull(resolveLocal(name));
    }

    default @NotNull Symbol resolveOrThrow(@NotNull String name) {
        return Objects.requireNonNull(resolve(name), name);
    }

    default @Nullable Symbol resolve(@NotNull SymbolPath<?> path) {
        return path.isAbs() ? resolve((AbsSymbolPath) path) : resolve((RelativeSymbolPath) path);
    }

    default @Nullable Symbol resolve(@NotNull RelativeSymbolPath path) {
        var currentScope = this;

        MAIN: while (currentScope.parent() != null) {
            var returnableScope = currentScope;

            currentScope = currentScope.parentOrThrow();

            for (var i = 0; i < path.parts.size(); i++) {
                final var nextScopeName = path.parts.get(i);

                if (!returnableScope.containsLocalName(nextScopeName)) continue MAIN;

                final var currentSymbol = returnableScope.resolve(nextScopeName);

                if (currentSymbol instanceof Scope scope) {
                    returnableScope = scope;
                    continue;
                }

                if (i == path.parts.size() - 1) return currentSymbol;
                continue MAIN;
            }

            return returnableScope;
        }

        return null;
    }

    default @Nullable Symbol resolve(@NotNull AbsSymbolPath path) {
        var currentScope = root();

        if (!currentScope.name().equals(path.parts.getFirst()))
            return null;

        for (var i = 1; i < path.parts.size(); i++) {
            final var nextScopeName = path.parts.get(i);

            if (!currentScope.containsLocalName(nextScopeName)) break;

            final var currentSymbol = currentScope.resolve(nextScopeName);

            if (currentSymbol instanceof Scope scope) {
                currentScope = scope;
                continue;
            }

            return i == path.parts.size() - 1 ? currentSymbol : null;
        }

        return currentScope;
    }

    default @NotNull Result<AbsSymbolPath, Errors> resolveTypePath(
            @NotNull RelativeSymbolPath typePath
    ) {
        final var resolvedSymbol = resolve(typePath);

        if (resolvedSymbol == null)
            return Result.err(Errors.UNRESOLVED_REFERENCE);

        if (resolvedSymbol instanceof ClassScope)
            return Result.ok(resolvedSymbol.absPath());

        return Result.err(Errors.NOT_A_TYPE);
    }

    void put(@NotNull String name, @NotNull Symbol symbol);

    default void put(@NotNull Symbol symbol) {
        put(symbol.name(), symbol);
    }

    void setParent(@NotNull Scope parent);

    @NotNull Set<String> childNames();

    boolean isEmpty();

    int childrenCount();

    @Override
    default @NotNull String prettyString() {
        return prettyPrinted(0, 3);
    }

    default @NotNull String prettyPrinted(int offset, int childrenOffset) {
        return String.format(
                "%s%s {%s%s%s};",
                " ".repeat(offset),
                prettyStringHeader(),
                isEmpty() ? "" : "\n",
                representedChildren(offset, childrenOffset),
                isEmpty() ? "" : "\n" + " ".repeat(offset)
        );
    }

    default @NotNull String representedChildren(int offset, int childrenOffset) {
        return childNames().stream().map(name -> {
            final var resolved = resolve(name);

            if (resolved instanceof Scope scope) return scope.prettyPrinted(offset + childrenOffset, childrenOffset);
            return " ".repeat(offset + childrenOffset) + resolveOrThrow(name).prettyString();
        }).collect(Collectors.joining("\n"));
    }

    default @NotNull Scope root() {
        var currentScope = this;

        while (currentScope.parent() != null)
            currentScope = currentScope.parentOrThrow();

        return currentScope;
    }

    enum ValueTypeUndefined {
        DOES_NOT_EXIST,
        NON_CLASS_SYMBOL
    }

    class ResolveTypeResult<T extends TokenType> {
        public final Status status;
        public final List<TypedToken<T>> invalidTokens;
        public final AbsSymbolPath successfullyResolvedPath;
        public final IncompletedSymbol<?, ?> incompletedSymbol;
        public final NamedSymbol expected, resolved;

        private ResolveTypeResult(
                @NotNull Status status,
                @Nullable List<TypedToken<T>> invalidTokens,
                @Nullable AbsSymbolPath successfullyResolvedPath,
                @Nullable IncompletedSymbol<?, ?> incompletedSymbol, NamedSymbol expected, NamedSymbol resolved
        ) {
            this.status = status;
            this.invalidTokens = invalidTokens;
            this.successfullyResolvedPath = successfullyResolvedPath;
            this.incompletedSymbol = incompletedSymbol;
            this.expected = expected;
            this.resolved = resolved;
        }

        public static @NotNull ResolveTypeResult<?> successful(@NotNull AbsSymbolPath successfullyResolvedPath) {
            return new ResolveTypeResult<>(
                    Status.OK,
                    null,
                    successfullyResolvedPath,
                    null,
                    null,
                    null
            );
        }

        public static @NotNull ResolveTypeResult<?> incompleteSymbol(@NotNull IncompletedSymbol<?, ?> incompletedSymbol) {
            return new ResolveTypeResult<>(
                    Status.INCOMPLETE_SYMBOL,
                    null,
                    null,
                    incompletedSymbol,
                    null,
                    null
            );
        }

        public static @NotNull ResolveTypeResult<Simple> undefinedType(@NotNull List<TypedToken<Simple>> typeTokens) {
            return new ResolveTypeResult<>(
                    Status.UNDEFINED_TYPE,
                    typeTokens,
                    null,
                    null,
                    null,
                    null
            );
        }

        public static <_T extends TokenType> @NotNull ResolveTypeResult<_T> undefinedValue(
                @NotNull List<TypedToken<_T>> typeTokens,
                @NotNull Scope.ResolveTypeResult.NamedSymbol expected
        ) {
            return new ResolveTypeResult<>(
                    Status.UNDEFINED_VALUE,
                    typeTokens,
                    null,
                    null,
                    expected,
                    null
            );
        }

        public static <_T extends TokenType> @NotNull ResolveTypeResult<_T> nonValue(
                @NotNull List<TypedToken<_T>> typeTokens,
                @NotNull Scope.ResolveTypeResult.NamedSymbol expected,
                @NotNull Scope.ResolveTypeResult.NamedSymbol resolved
        ) {
            return new ResolveTypeResult<>(
                    Status.NON_VALUE,
                    typeTokens,
                    null,
                    null,
                    expected,
                    resolved
            );
        }

        public enum Status {
            INCOMPLETE_SYMBOL,
            OK,
            UNDEFINED_TYPE,
            NON_VALUE,
            UNDEFINED_VALUE
        }

        public enum NamedSymbol {
            FUNCTION,
            FIELD,
            CLASS;

            public static @NotNull Scope.ResolveTypeResult.NamedSymbol of(@NotNull Symbol symbol) {
                return switch (symbol) {
                    case ClassScope _ -> CLASS;
                    case FuncSignaturesScope _, FuncScope<?> _ -> FUNCTION;
                    case FieldSymbol<?> _ -> FIELD;
                    default -> throw new IllegalStateException();
                };
            }
        }
    }
}
