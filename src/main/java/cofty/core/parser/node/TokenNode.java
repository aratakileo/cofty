package cofty.core.parser.node;

import cofty.core.lexer.token.type.TokenType;
import cofty.core.lexer.token.type.Simple;
import cofty.core.parser.ParseContext;
import cofty.type.Representable;
import cofty.core.parser.node.modifier.ModifierType;
import cofty.core.parser.node.modifier.NodeModifier;
import org.jetbrains.annotations.NotNull;

public class TokenNode extends EmptyNode {
    public final TokenType tokenType;

    public TokenNode(@NotNull TokenType tokenType, @NotNull NodeModifier modifier) {
        super(modifier);
        this.tokenType = tokenType;
    }

    private boolean compare(@NotNull ParseContext context) {
        if (!context.hasCurrent()) return false;

        if (context.currentOrThrow().type.equals(Simple.NEWLINE)) {
            if (tokenType.equals(Simple.NEWLINE)) return true;
            if (!context.hasNext()) return false;
            context.skipNewLine();
        }

        return context.currentOrThrow().type.equals(tokenType);
    }

    private boolean postProceed(
            @NotNull ParseContext context,
            @NotNull NodeModifier topLevelModifier,
            boolean proceed
    ) {
        if (proceed) {
            context.goNext();
            return true;
        }

        if (!topLevelModifier.is(ModifierType.GENERAL) || topLevelModifier.is(ModifierType.PREVIEW)) return false;

        if (modifier.is(ModifierType.FAIL))
            context.CRITICAL_MESSAGES.putErr(modifier.failMessageOrThrow());
        else if (modifier.is(ModifierType.GENERAL))
            context.goNext();

        return false;
    }

    @Override
    public boolean proceed(@NotNull ParseContext context, @NotNull NodeModifier topLevelModifier) {
        var proceeded = postProceed(
                context,
                topLevelModifier,
                compare(context)
        );

        if (proceeded && modifier.is(ModifierType.CONSUME) && !topLevelModifier.is(ModifierType.PREVIEW))
            modifier.consumerOrThrow().consume(context.prevOrThrow());

        return proceeded;
    }

    @Override
    public @NotNull String toReprString() {
        return String.format(
                "%s.token(%s, %s)",
                ParserNode.class.getSimpleName(),
                Representable.repr(tokenType),
                Representable.repr(modifier)
        );
    }
}
