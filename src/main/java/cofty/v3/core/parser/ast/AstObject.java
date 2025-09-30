package cofty.v3.core.parser.ast;

import cofty.core.parser.ParseContext;
import cofty.v3.core.parser.node.ParserNode;
import cofty.v3.core.parser.node.modifier.NodeModifier;
import org.jetbrains.annotations.NotNull;

public interface AstObject {
    @NotNull ParserNode parserNode();

    default boolean parse(@NotNull ParseContext context) {
        return parserNode().proceedQueue(context, NodeModifier.general());
    }
}
