package cofty.v2.core.semantics;

import cofty.v2.core.ast.object.BodyAst;
import cofty.core.message.MessageHandler;
import cofty.v2.core.semantics.namespace.BodyContainerObject;
import cofty.type.TextContent;
import org.jetbrains.annotations.NotNull;

public class SemanticAnalyzer {
    private final TextContent text;
    private final MessageHandler messages;
    private final BodyAst rootAst;
    private final BodyContainerObject rootNamespace;

    public SemanticAnalyzer(
            @NotNull TextContent text,
            @NotNull MessageHandler messages,
            @NotNull BodyAst rootAst,
            @NotNull BodyContainerObject rootNamespace
    ) {
        this.text = text;
        this.messages = messages;
        this.rootAst = rootAst;
        this.rootNamespace = rootNamespace;
    }

    public void analyze() {

    }
}
