package cofty.core.parser.ast;

import cofty.core.lexer.token.TypedToken;
import cofty.core.lexer.token.type.Simple;
import cofty.core.parser.ast.body.BodyObject;
import cofty.core.parser.ast.body.BodyResidentObject;
import org.jetbrains.annotations.NotNull;

import java.text.MessageFormat;
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

    @Override
    public @NotNull String prettyString(@NotNull String offset, int increase) {

        return MessageFormat.format(
                "{2}define class `{0}` '{'\n{1}\n{2}'}'",
                name.content,
                body.prettyString(offset.length() + increase, increase),
                offset
        );
    }
}
