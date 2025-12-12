package cofty.core.semantics.symbol;

import cofty.core.lexer.token.TypedToken;
import cofty.core.lexer.token.type.Simple;
import cofty.core.parser.ast.body.BodyResidentObject;
import cofty.core.semantics.symbol.path.AbsSymbolPath;
import cofty.core.semantics.symbol.scope.Scope;
import cofty.util.Cast;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public interface IncompletedSymbol<T extends BodyResidentObject, R extends Symbol> extends Symbol {
    @NotNull T basedOn();
    @NotNull CompletionResult<R> tryComplete();

    class CompletionResult<R extends Symbol> {
        public final Status status;
        public final @NotNull List<TypedToken<?>> invalidTokens;
        public final AbsSymbolPath notSuitableTypePath;
        public final Scope.ResolveTypeResult.NamedSymbol expected, resolved;
        public final R completedSymbol;

        private CompletionResult(
                @NotNull Status status,
                @Nullable List<TypedToken<?>> invalidTokens,
                @Nullable AbsSymbolPath notSuitableTypePath,
                @Nullable Scope.ResolveTypeResult.NamedSymbol expected,
                @Nullable Scope.ResolveTypeResult.NamedSymbol resolved,
                @Nullable R completedSymbol
        ) {
            this.status = status;
            this.invalidTokens = invalidTokens;
            this.notSuitableTypePath = notSuitableTypePath;
            this.expected = expected;
            this.resolved = resolved;
            this.completedSymbol = completedSymbol;
        }

        public @NotNull R completedSymbolOrThrow() {
            return Objects.requireNonNull(completedSymbol);
        }

        public static <R extends Symbol> @NotNull CompletionResult<R> OK(@NotNull R completedSymbol) {
            return new CompletionResult<>(Status.OK, null, null, null, null, completedSymbol);
        }

        public static <R extends Symbol> @NotNull CompletionResult<R> typeBasedFail(
                @NotNull Status status,
                @NotNull List<TypedToken<Simple>> typeTokens
        ) {
            if (status != Status.NON_TYPE && status != Status.UNDEFINED_TYPE)
                throw new IllegalArgumentException();

            return new CompletionResult<>(status, Cast.quiet(typeTokens), null, null, null, null);
        }

        public static <R extends Symbol> @NotNull CompletionResult<R> undefinedType(@NotNull List<TypedToken<Simple>> typeTokens) {
            return new CompletionResult<>(Status.UNDEFINED_TYPE, Cast.quiet(typeTokens), null, null, null, null);
        }

        public static <R extends Symbol> @NotNull CompletionResult<R> nonType(@NotNull List<TypedToken<Simple>> typeTokens) {
            return new CompletionResult<>(Status.NON_TYPE, Cast.quiet(typeTokens), null, null, null, null);
        }

        public static <R extends Symbol> @NotNull CompletionResult<R> undefinedValue(
                @NotNull List<TypedToken<?>> typeTokens,
                @NotNull Scope.ResolveTypeResult.NamedSymbol expected
        ) {
            return new CompletionResult<>(Status.UNDEFINED_VALUE, typeTokens, null, null, null, null);
        }

        public static <R extends Symbol> @NotNull CompletionResult<R> nonValue(
                @NotNull List<TypedToken<?>> typeTokens,
                @NotNull Scope.ResolveTypeResult.NamedSymbol expected,
                @NotNull Scope.ResolveTypeResult.NamedSymbol resolved
        ) {
            return new CompletionResult<>(Status.NON_VALUE, typeTokens, null, expected, resolved, null);
        }

        public static <R extends Symbol> @NotNull CompletionResult<R> notSuitableValueType(
                @NotNull List<TypedToken<?>> typeTokens,
                @NotNull AbsSymbolPath notSuitableTypePath
        ) {
            return new CompletionResult<>(Status.NOT_SUITABLE_VALUE_TYPE, typeTokens, notSuitableTypePath, null, null, null);
        }

        public static <R extends Symbol> @NotNull CompletionResult<R> failOf(@NotNull Scope.ResolveTypeResult<?> result) {
            return switch (result.status) {
                case UNDEFINED_TYPE -> undefinedType(Cast.quiet(result.invalidTokens));
                case NON_VALUE -> nonValue(Cast.quiet(result.invalidTokens), result.expected, result.resolved);
                case UNDEFINED_VALUE -> undefinedValue(Cast.quiet(result.invalidTokens), result.expected);
                default -> throw new IllegalStateException();
            };
        }

        public enum Status {
            OK,
            UNDEFINED_TYPE,
            NON_TYPE,
            UNDEFINED_VALUE,
            NON_VALUE,
            NOT_SUITABLE_VALUE_TYPE;

            public static @NotNull Status of(@NotNull Scope.ValueTypeUndefined valueTypeUndefined) {
                return switch (valueTypeUndefined) {
                    case DOES_NOT_EXIST -> UNDEFINED_TYPE;
                    case NON_CLASS_SYMBOL -> NON_TYPE;
                };
            }
        }
    }
}
