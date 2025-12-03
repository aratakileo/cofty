package cofty.v4.core.parser.ast;

import cofty.core.lexer.token.TypedToken;
import cofty.core.lexer.token.type.Simple;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class FuncDeclarationObject implements BodyResidentObject {
    public final TypedToken<Simple> name;
    public final List<FieldDeclarationObject> args;
    public final BodyObject body;
    public final TypeDescriptionObject returnType;

    public FuncDeclarationObject(
            @NotNull TypedToken<Simple> name,
            @NotNull List<FieldDeclarationObject> args,
            @NotNull BodyObject body,
            @Nullable TypeDescriptionObject returnType
    ) {
        this.name = name;
        this.args = args;
        this.body = body;
        this.returnType = returnType;
    }
}
