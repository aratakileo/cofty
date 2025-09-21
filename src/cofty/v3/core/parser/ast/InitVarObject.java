package cofty.v3.core.parser.ast;

import cofty.core.lexer.token.*;
import cofty.v3.core.parser.ast.value.ValueExpressionObject;
import cofty.v3.core.parser.node.ParserNode;
import cofty.v3.core.parser.node.TokenNode;
import cofty.v3.core.parser.node.modifier.NodeModifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InitVarObject implements AstObject {
    private Token name = null, mutable = null, explicitlySpecifiedType = null;

    private final ValueExpressionObject value = new ValueExpressionObject();

    private void setMutable(@NotNull Token mutable) {
        this.mutable = mutable;
    }

    private void setName(@NotNull Token name) {
        this.name = name;
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

    public @Nullable ValueExpressionObject value() {
        return value;
    }

    public @Nullable Token explicitlySpecifiedType() {
        return explicitlySpecifiedType;
    }

    @Override
    public @NotNull ParserNode parserNode() {
        var node = (TokenNode)null;

        (node = ParserNode.token(
                Keyword.LET,
                NodeModifier.previewAndGeneral())
        ).thenToken(Keyword.MUT, NodeModifier.builder().peek().tokenAction(this::setMutable).build())
                .thenToken(
                        TokenType.ID,
                        NodeModifier.builder()
                                .syntaxFail("expected a variable name")
                                .tokenAction(this::setName)
                                .build()
                ).thenToken(Separator.COLON, NodeModifier.peek())
                .thenToken(
                        TokenType.ID,
                        NodeModifier.builder()
                                .depended()
                                .syntaxFail("expected a variable value type")
                                .tokenAction(this::setExplicitlySpecifiedType)
                                .build()
                ).thenToken(Operator.ASSIGN, NodeModifier.peek())
                .then(
                        value.parserNode(),
                        NodeModifier.builder()
                                .depended()
                                .syntaxFail("expected a variable value")
                                .build()
                );

        return node;
    }

    @Override
    public String toString() {
        return "InitVarObject{" +
                "name=" + name +
                ", mutable=" + mutable +
                ", explicitlySpecifiedType=" + explicitlySpecifiedType +
                ", value=" + value +
                '}';
    }
}
