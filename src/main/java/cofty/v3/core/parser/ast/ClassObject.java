package cofty.v3.core.parser.ast;

import cofty.core.lexer.token.Keyword;
import cofty.core.lexer.token.TokenType;
import cofty.core.lexer.token.TypedToken;
import cofty.v3.core.parser.node.ParserNode;
import cofty.v3.core.parser.node.modifier.NodeModifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClassObject implements AstObject, WithModifiers, WithBody {
    private TypedToken<TokenType> name = null;

    private final ModifiersObject modifiers = new ModifiersObject();
    private final BodyObject body = new BodyObject();

    private void setName(@NotNull TypedToken<?> name) {
        this.name = name.unsafeAs();
    }

    public @Nullable TypedToken<TokenType> name() {
        return name;
    }

    public @NotNull ModifiersObject modifiers() {
        return modifiers;
    }

    public @NotNull BodyObject body() {
        return body;
    }

    @Override
    public @NotNull ParserNode parserNode() {
        var node = (ParserNode)null;

        (node = modifiers.parserNode())
                .thenToken(Keyword.CLS, NodeModifier.generalAndPreview())
                .thenToken(
                        TokenType.ID,
                        NodeModifier.builder().syntaxFail("expected a class name").tokenConsumer(this::setName).build()
                ).then(body.multilineSubbody(
                        NodeModifier.syntaxFail("expected a class body description"),
                        NodeModifier.syntaxFail("expected an end of class body description")
                ));

        return node;
    }

    @Override
    public String toString() {
        return "ClassObject{" +
                "name=" + name +
                ", modifiers=" + modifiers +
                ", body=" + body +
                '}';
    }
}
