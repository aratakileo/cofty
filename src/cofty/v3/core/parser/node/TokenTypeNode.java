package cofty.v3.core.parser.node;

import cofty.core.parser.ParseContext;
import cofty.core.lexer.token.ITokenType;
import cofty.type.Representable;
import cofty.v3.core.parser.node.modifier.NodeModifier;
import org.jetbrains.annotations.NotNull;

public class TokenTypeNode extends EmptyNode {
    public final ITokenType tokenType;

    public TokenTypeNode(@NotNull ITokenType tokenType, @NotNull NodeModifier modifier) {
        super(modifier);
        this.tokenType = tokenType;
    }

    @Override
    public boolean proceed(@NotNull ParseContext context, @NotNull NodeModifier topLevelModifier) {
        var proceeded = postProceed(
                context.hasCurrent() && context.currentOrThrow().type.equals(tokenType),
                context,
                topLevelModifier
        );

        if (proceeded && modifier.isAction() && !modifier().isPreviewAnchor())
            modifier.actionOrThrow().applyToken(context.peekOrThrow(-1));

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
