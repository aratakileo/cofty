package cofty.v3.core.parser.ast;

import cofty.core.token.Keyword;
import cofty.core.token.Operator;
import cofty.core.token.Token;
import cofty.core.token.TokenType;
import cofty.v3.core.parser.node.ParserNode;
import cofty.v3.core.parser.node.TokenTypeNode;
import cofty.v3.core.parser.node.modifier.NodeModifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InitVarObject implements AstObject {
    private Token name = null, mutable = null, value = null;

    private void setMutable(@NotNull Token mutable) {
        this.mutable = mutable;
    }

    private void setName(@NotNull Token name) {
        this.name = name;
    }

    private void setValue(@NotNull Token value) {
        this.value = value;
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

    @Override
    public @NotNull ParserNode parserNode() {
        TokenTypeNode node;

        (node = ParserNode.tokenOrSyntaxFail(Keyword.LET))
                .thenToken(Keyword.MUT, NodeModifier.peekAndAction(this::setMutable))
                .thenToken(TokenType.ID, NodeModifier.syntaxFailAndAction(this::setName, "expected name"))
                .thenToken(Operator.ASSIGN, NodeModifier.syntaxFail("expected assign operator"))
                .thenToken(TokenType.INT, NodeModifier.syntaxFailAndAction(this::setValue, "expected value"));

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
