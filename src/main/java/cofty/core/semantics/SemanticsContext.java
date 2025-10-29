package cofty.core.semantics;

import cofty.v4.core.compiler.message.CompilationMessageHandler;
import cofty.type.TextContent;
import org.jetbrains.annotations.NotNull;

public class SemanticsContext {
    public final TextContent text;
    public final CompilationMessageHandler.TextAssociated messages;

    public SemanticsContext(@NotNull TextContent text, @NotNull CompilationMessageHandler messages) {
        this.text = text;
        this.messages = messages.associateWith(text);
    }
}
