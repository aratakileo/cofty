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

public class FuncDeclarationObject implements AstObject {
    private Token name = null, returnableType = null;

    private List<@NotNull FuncArgDescriptionObject> args = null;

    private final BodyObject body = new BodyObject(false);

    private void setName(@NotNull Token name) {
        this.name = name;
    }

    private void setReturnableType(@NotNull Token returnableType) {
        this.returnableType = returnableType;
    }

    private void setArgs(@NotNull List<AstObject> args) {
        for (final var arg: args)
            if (!(arg instanceof FuncArgDescriptionObject)) throw new IllegalStateException();

        this.args = Cast.unsafe(args);
    }

    public @Nullable Token name() {
        return name;
    }

    public @Nullable Token returnableType() {
        return returnableType;
    }

    public @Nullable List<@NotNull FuncArgDescriptionObject> args() {
        return args;
    }

    public @NotNull BodyObject body() {
        return body;
    }

    @Override
    public @NotNull ParserNode parserNode() {
        var node = (TokenNode)null;

        (node = ParserNode.token(Keyword.FN, NodeModifier.previewAndGeneral()))
                .thenToken(
                        TokenType.ID,
                        NodeModifier.builder()
                                .syntaxFail("expected a function name")
                                .tokenConsumer(this::setName)
                                .build()
                ).thenToken(
                        Brackets.ROUND_LEFT,
                        NodeModifier.syntaxFail("expected a function arguments description")
                )
                .thenRepeatableQueueBuilder(
                        NodeModifier.builder()
                                .astObjectsConsumer(this::setArgs)
                                .syntaxFail("invalid syntax")
                                .build()
                ).setSeparator(ParserNode.token(
                        Separator.COMMA,
                        NodeModifier.builder().syntaxFail("expected a comma separator").preview().build()
                )).makeSeparatorOnlyOneAtTime(new SyntaxError("duplicate comma"))
                .setStopper(ParserNode.token(Brackets.ROUND_RIGHT, NodeModifier.previewAndGeneral()))
                    .add(FuncArgDescriptionObject::new)
                    .build()
                .thenToken(Brackets.ROUND_RIGHT, NodeModifier.syntaxFail("expected an end of function arguments description"))
                .thenToken(Separator.ARROW, NodeModifier.peek())
                .thenToken(
                        TokenType.ID,
                        NodeModifier.builder()
                                .depended()
                                .syntaxFail("expected a function returnable type")
                                .tokenConsumer(this::setReturnableType)
                                .build()
                ).thenToken(Brackets.CURVE_LEFT, NodeModifier.syntaxFail("expected a function body"))
                .then(body.parserNode())
                .thenToken(Brackets.CURVE_RIGHT, NodeModifier.syntaxFail("expected an end of function body description"));

        return node;
    }

    @Override
    public String toString() {
        return "FuncDeclarationObject{" +
                "name=" + Representable.repr(name) +
                ", returnableType=" + returnableType +
                ", args=" + Representable.repr(args) +
                ", body=" + body +
                '}';
    }
}
