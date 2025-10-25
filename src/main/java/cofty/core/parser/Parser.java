package cofty.core.parser;

import cofty.core.parser.ast.BodyObject;
import cofty.core.parser.node.modifier.NodeModifier;
import org.jetbrains.annotations.NotNull;

@Deprecated
public class Parser {
    public final BodyObject bodyObject = new BodyObject();
    public final ParseContext context;

    public Parser(@NotNull ParseContext context) {
        this.context = context;
    }

    public boolean parse() {
        if (!bodyObject.parserNode().proceedQueue(context, NodeModifier.general())) return false;

        if (context.hasCurrent()) {
            context.CRITICAL_MESSAGES.putSyntaxErr("invalid syntax");
            return false;
        }

        return true;
    }
}
