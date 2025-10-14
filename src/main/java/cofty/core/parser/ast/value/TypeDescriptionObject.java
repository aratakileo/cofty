package cofty.core.parser.ast.value;

import cofty.core.parser.ast.AstObject;
import cofty.core.parser.node.ParserNode;
import cofty.core.parser.node.modifier.NodeModifier;
import org.jetbrains.annotations.NotNull;

public class TypeDescriptionObject implements AstObject {
    private final ComplexNameObject name = new ComplexNameObject();

    public @NotNull ComplexNameObject name() {
        return name;
    }

    @Override
    public @NotNull ParserNode parserNode() {
        return name.parserNode(NodeModifier.general());
    }

    public @NotNull ParserNode parserNode(@NotNull NodeModifier modifier) {
        return name.parserNode(modifier);
    }

    @Override
    public String toString() {
        return "TypeDescriptionObject{" +
                "name=" + name +
                '}';
    }
}
