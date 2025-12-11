package cofty.core.parser.ast;

import cofty.core.lexer.token.TypedToken;
import cofty.core.lexer.token.type.Simple;
import cofty.core.parser.ast.body.BodyObject;
import cofty.core.parser.ast.body.BodyResidentObject;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class ClassDeclarationObject implements DeclarationObject, WithBody, WithName {
    public final TypedToken<Simple> name;
    public final BodyObject body;

    public ClassDeclarationObject(@NotNull TypedToken<Simple> name, @NotNull BodyObject body) {
        this.name = name;
        this.body = body;
    }

    @Override
    public @NotNull List<BodyResidentObject> residents() {
        return body.residents;
    }

    @Override
    public @NotNull TypedToken<Simple> nameToken() {
        return name;
    }
}
