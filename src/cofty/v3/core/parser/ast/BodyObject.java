package cofty.v3.core.parser.ast;

import cofty.core.lexer.token.Brackets;
import cofty.core.lexer.token.TokenType;
import cofty.type.Representable;
import cofty.v3.core.parser.node.ParserNode;
import cofty.v3.core.parser.node.modifier.NodeModifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BodyObject implements AstObject {
    private final boolean isRootBody;

    private List<AstObject> objects = null;

    public BodyObject(boolean isRootBody) {
        this.isRootBody = isRootBody;
    }

    private void setObjects(@NotNull List<AstObject> objects) {
        this.objects = objects;
    }

    public @Nullable List<AstObject> objects() {
        return objects;
    }

    @Override
    public @NotNull ParserNode parserNode() {
        final var builder = ParserNode.repeatableQueueBuilder(
                    NodeModifier.builder()
                            .general()
                            .preview()
                            .astObjectsAction(this::setObjects)
                            .build()
                ).setSeparator(ParserNode.token(TokenType.NEWLINE, NodeModifier.previewAndGeneral()))
                .add(InitVarObject::new, SetVarObject::new);

        if (!isRootBody) builder.setStopper(ParserNode.token(Brackets.CURVE_RIGHT, NodeModifier.previewAndGeneral()));

        return builder.build();
    }

    @Override
    public String toString() {
        return "BodyObject{" +
                "objects=" + Representable.repr(objects) +
                '}';
    }
}
