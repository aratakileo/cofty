package cofty.v3.core.parser.ast;

import cofty.core.lexer.token.Keyword;
import cofty.core.lexer.token.Token;
import cofty.core.lexer.token.TokenType;
import cofty.v3.core.parser.node.ParserNode;
import cofty.v3.core.parser.node.modifier.NodeModifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClassObject implements AstObject {
    private Token name = null;

    private final ModifiersObject modifiers = new ModifiersObject();
    private final BodyObject body = new BodyObject();

    private void setName(@NotNull Token name) {
        this.name = name;
    }

    public @Nullable Token name() {
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
