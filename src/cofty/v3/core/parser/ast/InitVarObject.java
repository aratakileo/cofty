package cofty.v3.core.parser.ast;

import cofty.core.parser.ParseContext;
import cofty.core.lexer.token.*;
import cofty.type.exception.SyntaxError;
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
                NodeModifier.previewAnchor(NodeModifier.syntaxFail("expected `let` keyword")))
        ).thenToken(Keyword.MUT, NodeModifier.peekAndAction(this::setMutable))
                .thenToken(TokenType.ID, NodeModifier.syntaxFailAndAction(this::setName, "expected variable name"))
                .thenToken(Separator.COLON, NodeModifier.peek())
                .thenToken(TokenType.ID, NodeModifier.dependedActionOrSyntaxFail(this::setExplicitlySpecifiedType, "expected type declaration"))
                .thenToken(Operator.ASSIGN, NodeModifier.peek())
                .thenToken(TokenType.INT, NodeModifier.dependedActionOrSyntaxFail(this::setValue, "expected variable value"));

        return node;
    }

    @Override
    public boolean parse(@NotNull ParseContext context) {
        var isSuccessfullyParsed = AstObject.super.parse(context);

        if (!isSuccessfullyParsed) return false;

        if (explicitlySpecifiedType == null && value == null) {
            context.NON_CRITICAL_MESSAGES.putErrAfterToken(
                    new SyntaxError("expected a variable value or an explicitly specified type"),
                    name
            );
            return false;
        }

        return true;
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
