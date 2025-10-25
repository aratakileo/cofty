package cofty.v4.core.parser.ast;

import cofty.core.lexer.token.TypedToken;
import cofty.core.lexer.token.type.Keyword;
import cofty.core.lexer.token.type.Simple;
import cofty.v4.core.parser.ast.value.ValueExpressionObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class FieldDeclarationObject implements BodyResidentObject {
    public final TypedToken<Keyword> mutable;
    public final TypedToken<Simple> name;
    public final TypeDescriptionObject explicitlySpecifiedType;
    public final ValueExpressionObject value;

    private FieldDeclarationObject(
            @Nullable TypedToken<Keyword> mutable,
            @NotNull TypedToken<Simple> name,
            @Nullable TypeDescriptionObject explicitlySpecifiedType,
            @Nullable ValueExpressionObject value
    ) {
        this.mutable = mutable;
        this.name = name;
        this.explicitlySpecifiedType = explicitlySpecifiedType;
        this.value = value;
    }

    public static @NotNull FieldDeclarationObject create(
            @Nullable TypedToken<Keyword> mutable,
            @NotNull TypedToken<Simple> name,
            @NotNull TypeDescriptionObject explicitlySpecifiedType
    ) {
        return new FieldDeclarationObject(mutable, name, explicitlySpecifiedType, null);
    }

    public static @NotNull FieldDeclarationObject create(
            @Nullable TypedToken<Keyword> mutable,
            @NotNull TypedToken<Simple> name,
            @NotNull ValueExpressionObject value
    ) {
        return new FieldDeclarationObject(mutable, name, null, value);
    }

    public static @NotNull FieldDeclarationObject create(
            @Nullable TypedToken<Keyword> mutable,
            @NotNull TypedToken<Simple> name,
            @NotNull TypeDescriptionObject explicitlySpecifiedType,
            @NotNull ValueExpressionObject value
    ) {
        return new FieldDeclarationObject(mutable, name, explicitlySpecifiedType, value);
    }
}
