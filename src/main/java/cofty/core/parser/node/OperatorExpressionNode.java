package cofty.core.parser.node;

import cofty.core.parser.ParseContext;
import cofty.core.parser.node.modifier.NodeModifier;
import cofty.type.Representable;
import org.jetbrains.annotations.NotNull;

public class OperatorExpressionNode extends EmptyNode {
    public OperatorExpressionNode(@NotNull NodeModifier modifier) {
        super(modifier);
    }

    @Override
    public boolean proceed(@NotNull ParseContext context, @NotNull NodeModifier topLevelModifier) {
        return proceed(context, topLevelModifier, true);
    }

    public boolean proceed(@NotNull ParseContext context, @NotNull NodeModifier topLevelModifier, boolean breakExpressionByNewLine) {
        return new OperatorExpressionParser(context, modifier, topLevelModifier, breakExpressionByNewLine).parse();
    }

    @Override
    public @NotNull String toReprString() {
        return String.format(
                "%s.operatorExpression(%s)",
                ParserNode.class.getSimpleName(),
                Representable.repr(modifier)
        );
    }
}
