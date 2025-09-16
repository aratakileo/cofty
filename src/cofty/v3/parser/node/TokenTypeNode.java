package cofty.v3.parser.node;

import cofty.core.parse.ParseContext;
import cofty.core.token.ITokenType;
import cofty.type.Representable;
import cofty.v3.parser.node.flag.NodeFlag;
import org.jetbrains.annotations.NotNull;

public class TokenTypeNode extends EmptyNode implements Representable {
    public final ITokenType tokenType;

    public TokenTypeNode(@NotNull ITokenType tokenType, @NotNull NodeFlag flag) {
        super(flag);
        this.tokenType = tokenType;
    }

    @Override
    public boolean proceed(@NotNull ParseContext context, @NotNull NodeFlag topLevelFlag) {
        return postProceed(
                context.hasCurrent() && context.currentOrThrow().type.equals(tokenType),
                context,
                topLevelFlag
        );
    }

    @Override
    public @NotNull String toReprString() {
        return String.format(
                "%s.token(%s, %s)",
                ParserNode.class.getSimpleName(),
                Representable.repr(tokenType),
                Representable.repr(flag)
        );
    }
}
