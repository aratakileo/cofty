package cofty.v3.core.parser.ast;

import cofty.core.lexer.token.Operator;
import cofty.core.lexer.token.Token;
import cofty.core.lexer.token.TokenType;
import cofty.type.Representable;
import cofty.v3.core.parser.node.ParserNode;
import cofty.v3.core.parser.node.TokenNode;
import cofty.v3.core.parser.node.modifier.NodeModifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SetVarObject implements AstObject {
    private Token name = null, value = null;

    private void setValue(@NotNull Token value) {
        this.value = value;
    }

    private void setName(@NotNull Token name) {
        this.name = name;
    }

    public @NotNull Token value() {
        return value;
    }

    public @Nullable Token name() {
        return name;
    }

    @Override
    public @NotNull ParserNode parserNode() {
        var node = (TokenNode)null;

        (node = ParserNode.token(TokenType.ID, NodeModifier.builder().general().tokenAction(this::setName).build()))
                .thenToken(Operator.ASSIGN, NodeModifier.previewAndGeneral())
                .thenToken(
                        TokenType.INT,
                        NodeModifier.builder()
                                .tokenAction(this::setValue)
                                .syntaxFail("expected variable value")
                                .build()
                );

        return node;
    }

    @Override
    public String toString() {
        return "SetVarObject{" +
                "name=" + Representable.repr(name) +
                ", value=" + value +
                '}';
    }
}
