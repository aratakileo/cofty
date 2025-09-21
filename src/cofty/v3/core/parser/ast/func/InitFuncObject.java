package cofty.v3.core.parser.ast.func;

import cofty.core.lexer.token.*;
import cofty.type.Representable;
import cofty.type.exception.SyntaxError;
import cofty.util.Cast;
import cofty.v3.core.parser.ast.AstObject;
import cofty.v3.core.parser.ast.BodyObject;
import cofty.v3.core.parser.node.ParserNode;
import cofty.v3.core.parser.node.TokenNode;
import cofty.v3.core.parser.node.modifier.NodeModifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class InitFuncObject implements AstObject {
    private Token name = null, returnableValueType = null;

    private List<@NotNull FuncArgDescrObject> args = null;

    private final BodyObject body = new BodyObject(false);

    private void setName(@NotNull Token name) {
        this.name = name;
    }

    private void setReturnableValueType(@NotNull Token returnableValueType) {
        this.returnableValueType = returnableValueType;
    }

    private void setArgs(@NotNull List<AstObject> args) {
        for (final var arg: args)
            if (!(arg instanceof FuncArgDescrObject)) throw new IllegalStateException();

        this.args = Cast.unsafe(args);
    }

    public @Nullable Token name() {
        return name;
    }

    public @Nullable Token returnableValueType() {
        return returnableValueType;
    }

    public @Nullable List<@NotNull FuncArgDescrObject> args() {
        return args;
    }

    @Override
    public @NotNull ParserNode parserNode() {
        var node = (TokenNode)null;

        (node = ParserNode.token(Keyword.FN, NodeModifier.previewAndGeneral()))
                .thenToken(
                        TokenType.ID,
                        NodeModifier.builder()
                                .syntaxFail("expected a function name")
                                .tokenAction(this::setName)
                                .build()
                ).thenToken(
                        Brackets.ROUND_LEFT,
                        NodeModifier.syntaxFail("expected a function arguments description")
                )
                .thenRepeatableQueueBuilder(
                        NodeModifier.builder()
                                .astObjectsAction(this::setArgs)
                                .syntaxFail("invalid syntax")
                                .build()
                ).setSeparator(ParserNode.token(
                        Separator.COMMA,
                        NodeModifier.builder().syntaxFail("expected a comma separator").preview().build()
                )).makeSeparatorOnlyOneAtTime(new SyntaxError("duplicate comma"))
                .setStopper(ParserNode.token(Brackets.ROUND_RIGHT, NodeModifier.previewAndGeneral()))
                    .add(FuncArgDescrObject::new)
                    .build()
                .thenToken(Brackets.ROUND_RIGHT, NodeModifier.syntaxFail("expected an end of function arguments description"))
                .thenToken(Separator.ARROW, NodeModifier.peek())
                .thenToken(
                        TokenType.ID,
                        NodeModifier.builder()
                                .depended()
                                .syntaxFail("expected a function returnable type")
                                .tokenAction(this::setReturnableValueType)
                                .build()
                ).thenToken(Brackets.CURVE_LEFT, NodeModifier.syntaxFail("expected a function body"))
                .then(body.parserNode())
                .thenToken(Brackets.CURVE_RIGHT, NodeModifier.syntaxFail("expected an end of function body description"));

        return node;
    }

    @Override
    public String toString() {
        return "InitFuncObject{" +
                "name=" + Representable.repr(name) +
                ", returnableValueType=" + returnableValueType +
                ", args=" + Representable.repr(args) +
                ", body=" + body +
                '}';
    }
}
