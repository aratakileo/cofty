package cofty.v3.core.parser.node;

import cofty.core.parser.ParseContext;
import cofty.type.Representable;
import cofty.v3.core.parser.node.modifier.ModifierType;
import cofty.v3.core.parser.node.modifier.NodeModifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RepeatedAnyOfNode extends EmptyNode {
    private final List<ParserNode> nodes;

    public RepeatedAnyOfNode(@NotNull List<ParserNode> nodes, @NotNull NodeModifier modifier) {
        super(modifier);
        this.nodes = nodes;

        if (modifier.is(ModifierType.ACTION)) throw new IllegalStateException();
    }

    @Override
    public boolean proceed(@NotNull ParseContext context, @NotNull NodeModifier topLevelModifier) {
        var isRepeatedAtLeastOnce = false;

        root: while (true) {
            for (final var node : nodes) {
                if (!node.previewQueue(context, topLevelModifier)) continue;
                if (topLevelModifier.is(ModifierType.PREVIEW)) return true;
                if (node.proceedQueue(context, NodeModifier.prioritize(topLevelModifier, modifier))) {
                    isRepeatedAtLeastOnce = true;
                    continue root;
                }
            }

            break;
        }

        if (!isRepeatedAtLeastOnce && modifier.is(ModifierType.FAIL))
            context.CRITICAL_MESSAGES.putErr(modifier.failMessageOrThrow());

        return isRepeatedAtLeastOnce;
    }

    @Override
    public @NotNull String toReprString() {
        return String.format(
                "%s.repeatedAnyOf(%s, %s)",
                ParserNode.class.getSimpleName(),
                Representable.repr(nodes),
                Representable.repr(modifier)
        );
    }
}
