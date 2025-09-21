package cofty.v3.core.parser.ast.value;

import cofty.core.lexer.token.Token;
import cofty.core.lexer.token.TokenType;
import cofty.v3.core.parser.ast.AstObject;
import cofty.v3.core.parser.node.ParserNode;
import cofty.v3.core.parser.node.modifier.NodeModifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PrimitiveValueObject implements AstObject {
    private Token value = null;

    private void setValue(@NotNull Token value) {
        this.value = value;
    }

    public @Nullable Token value() {
        return value;
    }

    @Override
    public @NotNull ParserNode parserNode() {
        return ParserNode.token(
                TokenType.INT,
                NodeModifier.builder()
                        .general()
                        .preview()
                        .tokenAction(this::setValue)
                        .build()
        );
    }

    @Override
    public String toString() {
        return "PrimitiveValueObject{" +
                "value=" + value +
                '}';
    }
}
