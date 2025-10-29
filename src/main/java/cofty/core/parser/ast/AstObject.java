package cofty.core.parser.ast;

import cofty.core.parser.ParseContext;
import cofty.core.parser.node.ParserNode;
import cofty.core.parser.node.modifier.NodeModifier;
import org.jetbrains.annotations.NotNull;

@Deprecated
public interface AstObject {
    @NotNull ParserNode parserNode();
}
