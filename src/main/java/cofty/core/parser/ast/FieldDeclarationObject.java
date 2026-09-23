package cofty.core.parser.ast;

import cofty.core.lexer.token.TypedToken;
import cofty.core.lexer.token.type.Keyword;
import cofty.core.lexer.token.type.Simple;
import cofty.core.parser.ast.value.ExpressionValueObject;
import cofty.core.parser.ast.value.complex.SimpleValueObject;
import cofty.core.semantics.symbol.TypeDescriptor;
import cofty.core.semantics.symbol.path.RelativeSymbolPath;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public final class FieldDeclarationObject implements DeclarationObject, WithName, WithDiagnosticFailAnchor {
    private final TypedToken<Keyword> keyword;

    public final TypedToken<Keyword> mutable;
    public final TypedToken<Simple> name;
    public final TypeDescriptorObject valueType;
    public final ExpressionValueObject value;

    private FieldDeclarationObject(
            @Nullable TypedToken<Keyword> keyword,
            @Nullable TypedToken<Keyword> mutable,
            @NotNull TypedToken<Simple> name,
            @Nullable TypeDescriptorObject valueType,
            @Nullable ExpressionValueObject value
    ) {
        this.keyword = keyword;
        this.mutable = mutable;
        this.name = name;
        this.valueType = valueType;
        this.value = value;
    }

    @Override
    public @NotNull TypedToken<Simple> nameToken() {
        return name;
    }

    @Override
    public @NotNull List<TypedToken<?>> failTokensRange() {
        return List.of(
                keyword != null ? keyword : (mutable != null ? mutable : name),
                value != null ? value.lastFailToken() : valueType.lastFailToken()
        );
    }

    public @NotNull ExpressionValueObject valueOrThrow() {
        return Objects.requireNonNull(value);
    }

    public @NotNull TypeDescriptor<RelativeSymbolPath> constantValueTypeOrThrow() {
        return valueType == null
                ? TypeDescriptor.rawReference(((SimpleValueObject)valueOrThrow()).valueTypeName())
                : TypeDescriptor.rawReference(valueType);
    }

    public static @NotNull FieldDeclarationObject createField(
            @NotNull TypedToken<Keyword> keyword,
            @Nullable TypedToken<Keyword> mutable,
            @NotNull TypedToken<Simple> name,
            @Nullable TypeDescriptorObject explicitlySpecifiedType,
            @Nullable ExpressionValueObject value
    ) {
        return new FieldDeclarationObject(keyword, mutable, name, explicitlySpecifiedType, value);
    }

    public static @NotNull FieldDeclarationObject createArgument(
            @Nullable TypedToken<Keyword> mutable,
            @NotNull TypedToken<Simple> name,
            @Nullable TypeDescriptorObject explicitlySpecifiedType,
            @Nullable ExpressionValueObject value
    ) {
        return new FieldDeclarationObject(null, mutable, name, explicitlySpecifiedType, value);
    }

    @Override
    public @NotNull String prettyString(@NotNull String offset, int increase) {
        final var result = new StringBuilder(offset);

        result.append("define ")
                .append(mutable == null ? "immutable" : "mutable")
                .append(" field [`")
                .append(name.content).append("`; ")
                .append(valueType == null ? "type as in value" : valueType.prettyString("", increase))
                .append(']');

        if (value != null)
            result.append(" := ").append(value.prettyString("", increase));

        return result.toString();
    }
}
