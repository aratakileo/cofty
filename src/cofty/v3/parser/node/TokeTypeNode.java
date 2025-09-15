package cofty.v3.parser.node;

import cofty.core.parse.ParseContext;
import cofty.core.token.ITokenType;
import cofty.v3.parser.node.flag.NodeFlag;
import org.jetbrains.annotations.NotNull;

public class TokeTypeNode extends EmptyNode {
    public final ITokenType tokenType;

    public TokeTypeNode(@NotNull ITokenType tokenType, @NotNull NodeFlag flag) {
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
}
