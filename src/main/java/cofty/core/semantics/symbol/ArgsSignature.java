package cofty.core.semantics.symbol;

import cofty.core.parser.ast.value.ExpressionValueObject;
import cofty.core.semantics.symbol.path.AbsSymbolPath;
import cofty.core.semantics.symbol.path.RelativeSymbolPath;
import cofty.core.semantics.symbol.path.SymbolPath;
import cofty.core.semantics.symbol.scope.Scope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class ArgsSignature<T extends SymbolPath<T>> {
    public final static String SIGNATURES_PREFIX = "signature:";
    public final static ArgsSignature<RelativeSymbolPath> EMPTY_RELATIVE = createEmpty();
    public final static ArgsSignature<AbsSymbolPath> EMPTY_ABSOLUTE = createEmpty();

    private final List<@NotNull TypeDescriptor<T>> types;
    private final List<@NotNull String> names;
    private final List<@Nullable ExpressionValueObject> defaults;
    private final List<@NotNull CompletedFieldSymbol> fields;

    public final String minimumSignature;
    public final int requiredArgsCount;

    private ArgsSignature(
            @Nullable List<@NotNull TypeDescriptor<T>> types,
            @Nullable List<@NotNull String> names,
            @Nullable List<@Nullable ExpressionValueObject> defaults,
            @Nullable List<@NotNull CompletedFieldSymbol> fields,
            @NotNull String minimumSignature,
            int requiredArgsCount
    ) {
        this.types = types;
        this.names = names;
        this.defaults = defaults;
        this.minimumSignature = minimumSignature;
        this.fields = fields;
        this.requiredArgsCount = requiredArgsCount;
    }

    public boolean containsDefaultArgs() {
        return types == null || requiredArgsCount != types.size();
    }

    public boolean containsName(@NotNull String name) {
        return names != null && names.contains(name);
    }

    public int size() {
        return types == null ? 0 : types.size();
    }

    public boolean isEmpty() {
        return types == null || types.isEmpty();
    }

    public @NotNull List<TypeDescriptor<T>> types() {
        return types == null ? List.of() : types;
    }

    public @Nullable ExpressionValueObject getDefaultValue(int i) {
        return defaults == null ? null : defaults.get(i);
    }

    public @NotNull TypeDescriptor<T> getType(int i) {
        return Objects.requireNonNull(types).get(i);
    }

    public @NotNull TypeDescriptor<T> getType(@NotNull String name) {
        return types.get(Objects.requireNonNull(names).indexOf(name));
    }

    public @NotNull String getName(int i) {
        return Objects.requireNonNull(names).get(i);
    }

    public @NotNull CompletedFieldSymbol getField(int i) {
        if (fields == null)
            throw new IllegalStateException();

        return fields.get(i);
    }

    public @NotNull CompletedFieldSymbol getField(@NotNull String name) {
        if (fields == null)
            throw new IllegalStateException();

        return fields.get(names.indexOf(name));
    }

    public @NotNull List<String> names() {
        return names == null ? List.of() : names;
    }

    public void setParent(@NotNull Scope scope) {
        if (types != null && fields == null)
            throw new IllegalStateException();

        if (fields == null) return;

        for (final var field: fields)
            field.setParent(scope);
    }

    public static @NotNull String viewOf(@NotNull List<? extends TypeDescriptor<?>> argsSignature) {
        return SIGNATURES_PREFIX + argsSignature.stream()
                .map(TypeDescriptor::fullName)
                .collect(Collectors.joining(";"));
    }

    public static @NotNull ArgsSignature<RelativeSymbolPath> create(
            @NotNull List<@NotNull TypeDescriptor<RelativeSymbolPath>> types,
            @NotNull List<@NotNull String> names,
            @NotNull List<@Nullable ExpressionValueObject> defaults,
            int requiredArgsCount
    ) {
        if (types.size() != names.size() || names.size() != defaults.size())
            throw new IllegalArgumentException();

        return new ArgsSignature<>(
                types.stream().toList(),
                names.stream().toList(),
                defaults.stream().toList(),
                null,
                viewOf(types.subList(0, requiredArgsCount)),
                requiredArgsCount
        );
    }

    public static @NotNull ArgsSignature<AbsSymbolPath> create(
            @NotNull List<@NotNull CompletedFieldSymbol> fields,
            @NotNull List<@Nullable ExpressionValueObject> defaults
    ) {
        if (fields.size() != defaults.size())
            throw new IllegalArgumentException();

        final var types = fields.stream().map(CompletedFieldSymbol::valueTypeOrThrow).toList();
        final var requiredArgsCount = Collections.frequency(defaults, null);

        return new ArgsSignature<>(
                types,
                fields.stream().map(CompletedFieldSymbol::name).toList(),
                defaults.stream().toList(),
                fields.stream().toList(),
                viewOf(types.subList(0, requiredArgsCount)),
                requiredArgsCount
        );
    }

    private static <T extends SymbolPath<T>> @NotNull ArgsSignature<T> createEmpty() {
        return new ArgsSignature<>(null, null, null, null, SIGNATURES_PREFIX, 0);
    }
}
