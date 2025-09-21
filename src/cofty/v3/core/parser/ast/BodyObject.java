package cofty.v3.core.parser.ast;

import cofty.core.lexer.token.Brackets;
import cofty.core.lexer.token.TokenType;
import cofty.type.Representable;
import cofty.v3.core.parser.ast.func.CallFuncObject;
import cofty.v3.core.parser.ast.func.InitFuncObject;
import cofty.v3.core.parser.ast.func.ReturnExpressionObject;
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
                        .preview()
                        .astObjectsAction(this::setObjects)
                        .syntaxFail("invalid syntax")
                        .build()
        ).setSeparator(ParserNode.token(
                TokenType.NEWLINE,
                NodeModifier.builder()
                        .syntaxFail("expected the new expression would start on a new line")
                        .preview()
                        .build()
        )).add(
                InitVarObject::new,
                SetVarObject::new,
                InitFuncObject::new,
                CallFuncObject::new,
                ReturnExpressionObject::new
        );

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
