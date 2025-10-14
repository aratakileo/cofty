package cofty.core.parser;

import cofty.core.parser.ast.BodyObject;
import cofty.type.exception.SyntaxError;
import org.jetbrains.annotations.NotNull;

public class Parser {
    public final BodyObject bodyObject = new BodyObject();
    public final ParseContext context;

    public Parser(@NotNull ParseContext context) {
        this.context = context;
    }

    public boolean parse() {
        if (!bodyObject.parse(context)) return false;

        if (context.hasCurrent()) {
            context.CRITICAL_MESSAGES.putErr(new SyntaxError("invalid syntax"));
            return false;
        }

        return true;
    }
}
