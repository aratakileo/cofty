package cofty.v3.core.parser.ast;

import cofty.core.lexer.token.*;
import cofty.v3.core.parser.node.ParserNode;
import cofty.v3.core.parser.node.TokenTypeNode;
import cofty.v3.core.parser.node.modifier.NodeModifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InitVarObject implements AstObject {
    private Token name = null, mutable = null, explicitlySpecifiedType = null, value = null;

    private void setMutable(@NotNull Token mutable) {
        this.mutable = mutable;
    }

    private void setName(@NotNull Token name) {
        this.name = name;
    }

    private void setValue(@NotNull Token value) {
        this.value = value;
    }

    private void setExplicitlySpecifiedType(@NotNull Token explicitlySpecifiedType) {
        this.explicitlySpecifiedType = explicitlySpecifiedType;
    }

    public @Nullable Token mutable() {
        return mutable;
    }

    public @Nullable Token name() {
        return name;
    }

    public @Nullable Token value() {
        return value;
    }

    public @Nullable Token explicitlySpecifiedType() {
        return explicitlySpecifiedType;
    }

    @Override
    public @NotNull ParserNode parserNode() {
        TokenTypeNode node;

        (node = ParserNode.token(
                Keyword.LET,
                NodeModifier.builder().preview().syntaxFail("expected `let` keyword").build())
        ).thenToken(Keyword.MUT, NodeModifier.builder().peek().tokenAction(this::setMutable).build())
                .thenToken(
                        TokenType.ID,
                        NodeModifier.builder()
                                .syntaxFail("expected variable name")
                                .tokenAction(this::setName)
                                .build()
                ).thenToken(Separator.COLON, NodeModifier.peek())
                .thenToken(
                        TokenType.ID,
                        NodeModifier.builder()
                                .depended()
                                .syntaxFail("expected type declaration")
                                .tokenAction(this::setExplicitlySpecifiedType)
                                .build()
                ).thenToken(Operator.ASSIGN, NodeModifier.peek())
                .thenToken(
                        TokenType.INT,
                        NodeModifier.builder()
                                .depended()
                                .syntaxFail("expected variable value")
                                .tokenAction(this::setValue)
                                .build()
                );

        return node;
    }

    @Override
    public String toString() {
        return "InitVarData{" +
                "name=" + name +
                ", mutable=" + mutable +
                ", value=" + value +
                '}';
    }
}
