package cofty.v3.parser.node;

import cofty.core.parse.ParseContext;
import cofty.v3.parser.node.flag.NodeFlag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class EmptyNode implements ParserNode {
    public final NodeFlag flag;

    private ParserNode next = null;

    public EmptyNode(@NotNull NodeFlag flag) {
        this.flag = flag;
    }

    @Override
    public @Nullable ParserNode next() {
        return next;
    }

    @Override
    public @NotNull NodeFlag flag() {
        return flag;
    }

    @Override
    public <E extends ParserNode> @NotNull E then(@NotNull E next) {
        this.next = next;
        return next;
    }

    protected boolean postProceed(boolean proceed, @NotNull ParseContext context, @NotNull NodeFlag topLevelFlag) {
        if (proceed) {
            context.next();
            return true;
        }

        if (!topLevelFlag.isGeneral()) return false;

        if (flag.isFailMessage())
            context.putErrorMessage(flag.failMessageOrThrow());
        else if (flag.isGeneral())
            context.next();

        return false;
    }
}
