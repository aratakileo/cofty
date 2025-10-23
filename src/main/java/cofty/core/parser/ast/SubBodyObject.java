package cofty.core.parser.ast;

import cofty.core.lexer.token.TypedToken;
import cofty.core.lexer.token.type.operator.Bracket;
import cofty.core.parser.node.ParserNode;
import cofty.core.parser.node.modifier.NodeModifier;
import org.jetbrains.annotations.NotNull;

public class SubBodyObject implements AstObject, WithBody, WithModifiers, WithAnchor<Bracket> {
    private TypedToken<Bracket> anchor = null;

    private final BodyObject body = new BodyObject();
    private final ModifiersObject modifiers = new ModifiersObject();

    private void setAnchor(@NotNull TypedToken<?> anchor) {
        this.anchor = anchor.strictAs();
    }

    @Override
    public @NotNull TypedToken<Bracket> anchor() {
        return anchor;
    }

    @Override
    public @NotNull BodyObject body() {
        return body;
    }

    @Override
    public @NotNull ModifiersObject modifiers() {
        return modifiers;
    }

    @Override
    public @NotNull ParserNode parserNode() {
        final var node = modifiers.parserNode();

        node.then(body.multilineSubbody(
                NodeModifier.builder().general().preview().tokenConsumer(this::setAnchor).build(),
                NodeModifier.syntaxFail("expected and end of sub body description")
        ));

        return node;
    }

    @Override
    public String toString() {
        return "SubBodyObject{" +
                "anchor=" + anchor +
                ", body=" + body +
                ", modifiers=" + modifiers +
                '}';
    }
}
