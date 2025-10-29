package cofty.core.parser.ast;

import cofty.core.lexer.token.type.Modifier;
import cofty.core.lexer.token.TypedToken;
import cofty.type.Representable;
import cofty.core.parser.node.ParserNode;
import cofty.core.parser.node.modifier.NodeModifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Deprecated
public class ModifiersObject implements AstObject {
    private final ArrayList<@NotNull TypedToken<Modifier>> modifiers = new ArrayList<>();

    private void addModifier(@NotNull TypedToken<?> modifierToken) {
        modifiers.add(modifierToken.strictAs());
    }

    public @NotNull List<@NotNull TypedToken<Modifier>> modifierTokens() {
        return modifiers.stream().toList();
    }

    @Override
    public @NotNull ParserNode parserNode() {
        final var nodes = Stream.of(Modifier.values()).map(
                type -> ParserNode.token(
                        type,
                        NodeModifier.builder()
                                .general()
                                .preview()
                                .setShadowPreview(true)
                                .tokenConsumer(this::addModifier)
                                .build()
                )
        ).toList();

        return ParserNode.repeatableAnyOf(NodeModifier.builder().peek().build(), nodes);
    }

    @Override
    public String toString() {
        return "ModifiersObject{" +
                "modifiers=" + Representable.repr(modifiers) +
                '}';
    }
}
