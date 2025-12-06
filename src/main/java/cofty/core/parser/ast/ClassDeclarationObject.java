package cofty.core.parser.ast;

import cofty.core.lexer.token.TypedToken;
import cofty.core.lexer.token.type.Simple;
import org.jetbrains.annotations.NotNull;

public final class ClassDeclarationObject implements DeclarationObject {
    public final TypedToken<Simple> name;
    public final BodyObject body;

    public ClassDeclarationObject(@NotNull TypedToken<Simple> name, @NotNull BodyObject body) {
        this.name = name;
        this.body = body;
    }
}
