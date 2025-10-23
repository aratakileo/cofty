package cofty.core.parser.node;

import cofty.core.parser.ParseContext;
import cofty.type.Representable;
import cofty.core.parser.node.modifier.ModifierType;
import cofty.core.parser.node.modifier.NodeModifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AnyOfNode extends EmptyNode {
    private final List<ParserNode> nodes;

    public AnyOfNode(@NotNull List<ParserNode> nodes, @NotNull NodeModifier modifier) {
        super(modifier);
        this.nodes = nodes;

        if (modifier.is(ModifierType.CONSUME)) throw new IllegalStateException();
    }

    @Override
    public boolean proceed(@NotNull ParseContext context, @NotNull NodeModifier topLevelModifier) {
        for (final var node: nodes) {
            if (!node.previewQueue(context, topLevelModifier)) continue;
            if (topLevelModifier.is(ModifierType.PREVIEW)) return true;
            if (node.proceedQueue(context, NodeModifier.prioritize(topLevelModifier, modifier))) return true;
            break;
        }

        if (modifier.is(ModifierType.FAIL)) context.CRITICAL_MESSAGES.putErr(modifier.failMessageOrThrow());
        return false;
    }

    @Override
    public @NotNull String toReprString() {
        return String.format(
                "%s.anyOf(%s, %s)",
                ParserNode.class.getSimpleName(),
                Representable.repr(nodes),
                Representable.repr(modifier)
        );
    }
}
