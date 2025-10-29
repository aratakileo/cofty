package cofty.core.parser.node;

import cofty.core.parser.ParseContext;
import cofty.type.Representable;
import cofty.core.parser.node.modifier.ModifierType;
import cofty.core.parser.node.modifier.NodeModifier;
import org.jetbrains.annotations.NotNull;

@Deprecated
public class ContainerNode extends EmptyNode {
    public final ParserNode node;

    public ContainerNode(@NotNull ParserNode node, @NotNull NodeModifier modifier) {
        super(modifier);
        this.node = node;

        if (modifier.is(ModifierType.CONSUME)) throw new IllegalStateException();
    }

    @Override
    public boolean proceed(@NotNull ParseContext context, @NotNull NodeModifier topLevelModifier) {
        final var isNodeProceeded = node.proceedQueue(context, NodeModifier.prioritize(topLevelModifier, modifier));

        if (isNodeProceeded) return true;
        if (modifier.is(ModifierType.FAIL)) context.CRITICAL_MESSAGES.putErr(modifier.failMessageOrThrow());

        return false;
    }

    @Override
    public @NotNull String toReprString() {
        return String.format(
                "%s.contain(%s, %s)",
                ParserNode.class.getSimpleName(),
                Representable.repr(node),
                Representable.repr(modifier)
        );
    }
}
