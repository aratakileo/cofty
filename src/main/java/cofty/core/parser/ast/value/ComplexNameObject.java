package cofty.core.parser.ast.value;

import cofty.core.lexer.token.TypedToken;
import cofty.core.lexer.token.type.operator.Separator;
import cofty.core.lexer.token.type.Simple;
import cofty.core.parser.ast.AstObject;
import cofty.core.parser.node.ParserNode;
import cofty.core.parser.node.modifier.NodeModifier;
import cofty.type.Representable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Deprecated
public class ComplexNameObject implements AstObject {
    private final ArrayList<TypedToken<Simple>> segments = new ArrayList<>();

    private void addSegment(@NotNull TypedToken<?> segment) {
        segments.add(segment.strictAs());
    }

    public List<TypedToken<Simple>> segments() {
        return segments.stream().toList();
    }

    @Override
    public @NotNull ParserNode parserNode() {
        return parserNode(NodeModifier.general());
    }

    public @NotNull ParserNode parserNode(@NotNull NodeModifier modifier) {
        modifier = NodeModifier.Builder.of(modifier).tokenConsumer(this::addSegment).build();

        final var node = ParserNode.token(Simple.WORD, modifier);

        node.thenRepeatedAnyOf(
                NodeModifier.peek(),
                ParserNode.token(Separator.DOT, NodeModifier.generalAndPreview(true))
                        .joinWithToken(
                                Simple.WORD,
                                NodeModifier.builder()
                                        .syntaxFail("invalid syntax")
                                        .tokenConsumer(this::addSegment)
                                        .build()
                        )
        );

        return node;
    }

    @Override
    public String toString() {
        return "ComplexNameObject{" +
                "segments=" + Representable.repr(segments) +
                '}';
    }
}
