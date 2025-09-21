package cofty.v3.core.parser.node;

import cofty.core.parser.ParseContext;
import cofty.type.Representable;
import cofty.v3.core.parser.node.modifier.ModifierType;
import cofty.v3.core.parser.node.modifier.NodeModifier;
import org.jetbrains.annotations.NotNull;

public class ContainerNode extends EmptyNode {
    public final ParserNode node;

    public ContainerNode(@NotNull ParserNode node, @NotNull NodeModifier modifier) {
        super(modifier);
        this.node = node;
    }

    @Override
    public boolean proceed(@NotNull ParseContext context, @NotNull NodeModifier topLevelModifier) {
        final var errorCursorStart = context.nonNewLineCursorOrPrev().start;

        if (node.proceedQueue(context, NodeModifier.prioritize(topLevelModifier, modifier))) return true;

        if (modifier.is(ModifierType.FAIL))
            context.CRITICAL_MESSAGES.putErr(modifier.failMessageOrThrow());

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
