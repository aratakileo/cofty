package cofty.core.parser.ast;

import cofty.core.lexer.token.type.Keyword;
import cofty.core.lexer.token.type.Simple;
import cofty.core.lexer.token.TypedToken;
import cofty.core.parser.node.ParserNode;
import cofty.core.parser.node.modifier.NodeModifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClassDeclarationObject implements AstObject, WithModifiers, WithBody, WithAnchor<Keyword> {
    private TypedToken<Keyword> anchor = null;
    private TypedToken<Simple> name = null;

    private final ModifiersObject modifiers = new ModifiersObject();
    private final BodyObject body = new BodyObject();

    private void setAnchor(@NotNull TypedToken<?> anchor) {
        this.anchor = anchor.strictAs();
    }

    private void setName(@NotNull TypedToken<?> name) {
        this.name = name.strictAs();
    }

    @Override
    public @NotNull TypedToken<Keyword> anchor() {
        return anchor;
    }

    public @Nullable TypedToken<Simple> name() {
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
                .thenToken(
                        Keyword.CLASS,
                        NodeModifier.builder().general().preview().tokenConsumer(this::setAnchor).build()
                ).thenToken(
                        Simple.WORD,
                        NodeModifier.builder().syntaxFail("expected a class name").tokenConsumer(this::setName).build()
                ).then(body.multilineSubbody(
                        NodeModifier.syntaxFail("expected a class body description"),
                        NodeModifier.syntaxFail("expected an end of class body description")
                ));

        return node;
    }

    @Override
    public String toString() {
        return "ClassDeclarationObject{" +
                "name=" + name +
                ", modifiers=" + modifiers +
                ", body=" + body +
                '}';
    }
}
