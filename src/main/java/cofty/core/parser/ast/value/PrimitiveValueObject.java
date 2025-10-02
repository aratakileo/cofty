package cofty.core.parser.ast.value;

import cofty.core.lexer.token.Keyword;
import cofty.core.lexer.token.TokenType;
import cofty.core.lexer.token.TypedToken;
import cofty.core.parser.ast.AstObject;
import cofty.core.parser.node.ParserNode;
import cofty.core.parser.node.modifier.NodeModifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public class PrimitiveValueObject implements AstObject {
    private TypedToken<?> value = null;

    private void setValue(@NotNull TypedToken<?> value) {
        this.value = value;
    }

    public @Nullable TypedToken<?> value() {
        return value;
    }

    @Override
    public @NotNull ParserNode parserNode() {
        final var nodes = Stream.of(
                TokenType.INT,
                TokenType.ID,
                TokenType.DOUBLE,
                TokenType.STR,
                Keyword.TRUE,
                Keyword.FALSE
        ).map(
                type -> ParserNode.token(
                        type,
                        NodeModifier.builder()
                                .general()
                                .preview()
                                .tokenConsumer(this::setValue)
                                .build()
                )
        ).toList();

        return ParserNode.anyOf(NodeModifier.generalAndPreview(), nodes);
    }

    @Override
    public String toString() {
        return "PrimitiveValueObject{" +
                "value=" + value +
                '}';
    }
}
