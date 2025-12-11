package cofty.core.semantics.symbol.scope;

import cofty.core.lexer.token.TypedToken;
import cofty.core.lexer.token.type.Simple;
import cofty.core.lexer.token.type.TokenType;
import cofty.core.parser.ast.TypeDescriptionObject;
import cofty.core.parser.ast.value.ExpressionValueObject;
import cofty.core.parser.ast.value.complex.FieldAccessObject;
import cofty.core.parser.ast.value.complex.FuncCallObject;
import cofty.core.parser.ast.value.complex.SimpleValueObject;
import cofty.core.semantics.symbol.*;
import cofty.core.semantics.symbol.path.AbsSymbolPath;
import cofty.core.semantics.symbol.path.RelativeSymbolPath;
import cofty.core.semantics.symbol.path.SymbolPath;
import cofty.type.Result;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public interface Scope extends Symbol {
    boolean containsName(@NotNull String name);

    @Nullable Symbol resolve(@NotNull String name);

    default @NotNull Symbol resolveOrThrow(@NotNull String name) {
        return Objects.requireNonNull(resolve(name));
    }

    default @Nullable Symbol resolve(@NotNull RelativeSymbolPath path) {
        var currentScope = this;

        MAIN: while (currentScope.parent() != null) {
            var returnableScope = currentScope;

            currentScope = currentScope.parentOrThrow();

            for (var i = 0; i < path.parts.size(); i++) {
                final var nextScopeName = path.parts.get(i);

                if (!returnableScope.containsName(nextScopeName)) continue MAIN;

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

            if (!currentScope.containsName(nextScopeName)) break;

            final var currentSymbol = currentScope.resolve(nextScopeName);

            if (currentSymbol instanceof Scope scope) {
                currentScope = scope;
                continue;
            }

            return i == path.parts.size() - 1 ? currentSymbol : null;
        }

        return currentScope;
    }

    default @NotNull Result<AbsSymbolPath, ValueTypeUndefined> resolveTypePath(
            @NotNull TypeDescriptionObject typeDescriptionObject
    ) {
        return resolveTypePath(RelativeSymbolPath.rawTokens(typeDescriptionObject.name));
    }

    default @NotNull Result<AbsSymbolPath, ValueTypeUndefined> resolveTypePath(
            @NotNull RelativeSymbolPath typePath
    ) {
        final var resolvedSymbol = resolve(typePath);

        if (resolvedSymbol == null)
            return Result.err(ValueTypeUndefined.DOES_NOT_EXIST);

        if (resolvedSymbol instanceof ClassScope)
            return Result.ok(resolvedSymbol.absPath());

        return Result.err(ValueTypeUndefined.NON_CLASS_SYMBOL);
    }

    default @NotNull ResolveTypeResult<?> resolveTypePath(@NotNull ExpressionValueObject valueObject) {
        return switch (valueObject) {
            case SimpleValueObject simpleValueObject -> {
                final var typeName = simpleValueObject.value.type.toString().toLowerCase();
                final var resolvedSymbol = resolve(typeName);

                if (resolvedSymbol == null)
                    throw new IllegalStateException();

                if (resolvedSymbol instanceof ClassScope)
                    yield ResolveTypeResult.successful(resolvedSymbol.absPath());

                throw new IllegalStateException();
            }

            case FuncCallObject funcCallObject -> {
                final var resolvedFunctionSymbol = resolve(funcCallObject.name.content);

                yield switch (resolvedFunctionSymbol) {
                    case null -> ResolveTypeResult.undefinedValue(
                            List.of(funcCallObject.name),
                            ResolveTypeResult.ValueType.FUNCTION
                    );
                    case FuncSignaturesScope funcSignaturesScope -> {
                        final var argsSignature = new ArrayList<AbsSymbolPath>();

                        for (final var argValue: funcCallObject.args) {
                            final var resolveTypeResult = resolveTypePath(argValue);

                            if (resolveTypeResult.status != ResolveTypeResult.Status.SUCCESSFUL)
                                yield resolveTypeResult;

                            argsSignature.add(resolveTypeResult.successfullyResolvedPath);
                        }

                        final var resolvedFunctionScope = funcSignaturesScope.resolve(argsSignature);

                        if (resolvedFunctionScope == null)
                            yield ResolveTypeResult.undefinedValue(
                                    List.of(funcCallObject.nameToken()),
                                    ResolveTypeResult.ValueType.FUNCTION
                            );

                        yield ResolveTypeResult.successful(resolvedFunctionScope.valueType());
                    }
                    case IncompletedSymbol<?, ?> incompletedSymbol -> ResolveTypeResult.incompleteSymbol(incompletedSymbol);
                    default -> ResolveTypeResult.nonValue(
                            List.of(funcCallObject.name),
                            ResolveTypeResult.ValueType.FUNCTION,
                            ResolveTypeResult.ValueType.of(resolvedFunctionSymbol)
                    );
                };
            }

            case FieldAccessObject fieldAccessObject -> {
                final var resolvedFieldSymbol = resolve(fieldAccessObject.name.content);

                yield switch (resolvedFieldSymbol) {
                    case null -> ResolveTypeResult.undefinedValue(
                            List.of(fieldAccessObject.name),
                            ResolveTypeResult.ValueType.FIELD
                    );
                    case CompletedFieldSymbol fieldSymbol -> ResolveTypeResult.successful(fieldSymbol.valueType());
                    case IncompletedSymbol<?, ?> incompletedSymbol -> ResolveTypeResult.incompleteSymbol(incompletedSymbol);
                    default -> ResolveTypeResult.nonValue(
                            List.of(fieldAccessObject.name),
                            ResolveTypeResult.ValueType.FIELD,
                            ResolveTypeResult.ValueType.of(resolvedFieldSymbol)
                    );
                };
            }

            default -> throw new IllegalStateException();
        };
    }

    void put(@NotNull String name, @NotNull Symbol symbol);

    default void put(@NotNull Symbol symbol) {
        put(symbol.name(), symbol);
    }

    void setParent(@NotNull Scope parent);

    @NotNull Set<String> childNames();

    @Override
    default @NotNull String represented() {
        return represented(0, 3);
    }

    default @NotNull String represented(int offset, int childrenOffset) {
        return String.format(
                "%s%s {%s%s%s};",
                " ".repeat(offset),
                representedHeader(),
                childNames().isEmpty() ? "" : "\n",
                representedChildren(offset, childrenOffset),
                childNames().isEmpty() ? "" : "\n" + " ".repeat(offset)
        );
    }

    default @NotNull String representedChildren(int offset, int childrenOffset) {
        return childNames().stream().map(name -> {
            final var resolved = resolve(name);

            if (resolved instanceof Scope scope) return scope.represented(offset + childrenOffset, childrenOffset);
            return " ".repeat(offset + childrenOffset) + resolveOrThrow(name).represented();
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
        public final ValueType expected, resolved;

        private ResolveTypeResult(
                @NotNull Status status,
                @Nullable List<TypedToken<T>> invalidTokens,
                @Nullable AbsSymbolPath successfullyResolvedPath,
                @Nullable IncompletedSymbol<?, ?> incompletedSymbol, ValueType expected, ValueType resolved
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
                    Status.SUCCESSFUL,
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
                @NotNull ValueType expected
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
                @NotNull ValueType expected,
                @NotNull ValueType resolved
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
            SUCCESSFUL,
            UNDEFINED_TYPE,
            NON_VALUE,
            UNDEFINED_VALUE
        }

        public enum ValueType {
            FUNCTION,
            FIELD,
            CLASS;

            public static @NotNull ValueType of(@NotNull Symbol symbol) {
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
