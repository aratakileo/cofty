package cofty.v3.core.parser.ast.func;

import cofty.core.lexer.token.*;
import cofty.v3.core.parser.ast.AstObject;
import cofty.v3.core.parser.node.ParserNode;
import cofty.v3.core.parser.node.TokenNode;
import cofty.v3.core.parser.node.modifier.NodeModifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FuncArgDescrObject implements AstObject {
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
        var node = (TokenNode)null;

        (node = ParserNode.token(Keyword.MUT, NodeModifier.builder().peek().tokenAction(this::setMutable).build()))
                .thenToken(
                        TokenType.ID,
                        NodeModifier.builder()
                                .syntaxFail("expected an argument name")
                                .preview()
                                .tokenAction(this::setName)
                                .build()
                ).thenToken(Separator.COLON, NodeModifier.peek())
                .thenToken(
                        TokenType.ID,
                        NodeModifier.builder()
                                .depended()
                                .syntaxFail("expected an argument value type")
                                .tokenAction(this::setExplicitlySpecifiedType)
                                .build()
                ).thenToken(Operator.ASSIGN, NodeModifier.peek())
                .thenToken(
                        TokenType.INT,
                        NodeModifier.builder()
                                .depended()
                                .syntaxFail("expected an argument value")
                                .tokenAction(this::setValue)
                                .build()
                );

        return node;
    }

    @Override
    public String toString() {
        return "FuncArgDescrObject{" +
                "name=" + name +
                ", mutable=" + mutable +
                ", explicitlySpecifiedType=" + explicitlySpecifiedType +
                ", value=" + value +
                '}';
    }
}
