package cofty.core.parser.ast;

import cofty.core.lexer.token.TypedToken;
import cofty.core.lexer.token.type.Keyword;
import cofty.core.lexer.token.type.Simple;
import cofty.core.parser.ast.value.ExpressionValueObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class FieldDeclarationObject implements DeclarationObject, WithName {
    public final TypedToken<Keyword> mutable;
    public final TypedToken<Simple> name;
    public final TypeDescriptorObject valueType;
    public final ExpressionValueObject value;

    private FieldDeclarationObject(
            @Nullable TypedToken<Keyword> mutable,
            @NotNull TypedToken<Simple> name,
            @Nullable TypeDescriptorObject valueType,
            @Nullable ExpressionValueObject value
    ) {
        this.mutable = mutable;
        this.name = name;
        this.valueType = valueType;
        this.value = value;
    }

    public @NotNull ExpressionValueObject valueOrThrow() {
        return Objects.requireNonNull(value);
    }

    public static @NotNull FieldDeclarationObject create(
            @Nullable TypedToken<Keyword> mutable,
            @NotNull TypedToken<Simple> name,
            @NotNull TypeDescriptorObject explicitlySpecifiedType
    ) {
        return new FieldDeclarationObject(mutable, name, explicitlySpecifiedType, null);
    }

    public static @NotNull FieldDeclarationObject create(
            @Nullable TypedToken<Keyword> mutable,
            @NotNull TypedToken<Simple> name,
            @NotNull ExpressionValueObject value
    ) {
        return new FieldDeclarationObject(mutable, name, null, value);
    }

    public static @NotNull FieldDeclarationObject create(
            @Nullable TypedToken<Keyword> mutable,
            @NotNull TypedToken<Simple> name,
            @NotNull TypeDescriptorObject explicitlySpecifiedType,
            @NotNull ExpressionValueObject value
    ) {
        return new FieldDeclarationObject(mutable, name, explicitlySpecifiedType, value);
    }

    @Override
    public @NotNull TypedToken<Simple> nameToken() {
        return name;
    }
}
