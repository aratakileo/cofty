package cofty.core.parser.ast.func;

import cofty.core.lexer.token.*;
import cofty.core.lexer.token.type.Brackets;
import cofty.core.lexer.token.type.Keyword;
import cofty.core.lexer.token.type.Separator;
import cofty.core.lexer.token.type.Simple;
import cofty.core.parser.ast.*;
import cofty.type.Representable;
import cofty.type.exception.SyntaxError;
import cofty.util.Cast;
import cofty.core.parser.node.ParserNode;
import cofty.core.parser.node.modifier.NodeModifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FuncDeclarationObject implements AstObject, WithBody, WithAnchor<Keyword> {
    private TypedToken<Keyword> anchor = null;
    private TypedToken<Simple> name = null, returnableType = null;
    private List<@NotNull VarDeclarationObject> args = null;

    private final ModifiersObject modifiers = new ModifiersObject();
    private final BodyObject body = new BodyObject();

    private void setAnchor(@NotNull TypedToken<?> anchor) {
        this.anchor = anchor.strictAs();
    }

    private void setName(@NotNull TypedToken<?> name) {
        this.name = name.strictAs();
    }

    private void setReturnableType(@NotNull TypedToken<?> returnableType) {
        this.returnableType = returnableType.strictAs();
    }

    private void setArgs(@NotNull List<AstObject> args) {
        for (final var arg: args)
            if (!(arg instanceof VarDeclarationObject)) throw new IllegalStateException();

        this.args = Cast.unsafe(args);
    }

    @Override
    public @NotNull TypedToken<Keyword> anchor() {
        return anchor;
    }

    public @Nullable TypedToken<Simple> name() {
        return name;
    }

    public @NotNull ModifiersObject modifiers() {
        return modifiers;
    }

    public @Nullable TypedToken<Simple> returnableType() {
        return returnableType;
    }

    public @Nullable List<@NotNull VarDeclarationObject> args() {
        return args;
    }

    public @NotNull BodyObject body() {
        return body;
    }

    @Override
    public @NotNull ParserNode parserNode() {
        var node = (ParserNode) null;

        (node = modifiers.parserNode())
                .thenToken(
                        Keyword.FUN,
                        NodeModifier.builder().general().preview().tokenConsumer(this::setAnchor).build()
                ).thenToken(
                        Simple.WORD,
                        NodeModifier.builder()
                                .syntaxFail("expected a function name")
                                .tokenConsumer(this::setName)
                                .build()
                ).thenToken(
                        Brackets.ROUND_OPEN,
                        NodeModifier.syntaxFail("expected a function arguments description")
                )
                .thenRepeatableQueueBuilder(
                        NodeModifier.builder()
                                .astObjectsConsumer(this::setArgs)
                                .syntaxFail("invalid syntax")
                                .build()
                )
                    .setSeparator(ParserNode.token(
                            Separator.COMMA,
                            NodeModifier.builder().syntaxFail("expected a comma separator").preview().build()
                    )).makeSeparatorOnlyOneAtTime(new SyntaxError("duplicate comma"))
                    .setStopper(ParserNode.token(Brackets.ROUND_CLOSE, NodeModifier.generalAndPreview()))
                    .add(() -> new VarDeclarationObject(true))
                    .build()
                .thenToken(
                        Brackets.ROUND_CLOSE,
                        NodeModifier.syntaxFail("expected an end of function arguments description")
                ).thenToken(Separator.ARROW, NodeModifier.peek())
                .thenToken(
                        Simple.WORD,
                        NodeModifier.builder()
                                .depended()
                                .syntaxFail("expected a function returnable type")
                                .tokenConsumer(this::setReturnableType)
                                .build()
                ).then(body.multilineSubbody(
                        NodeModifier.syntaxFail("expected a function body"),
                        NodeModifier.syntaxFail("expected an end of function body description")
                ));

        return node;
    }

    @Override
    public String toString() {
        return "FuncDeclarationObject{" +
                "name=" + Representable.repr(name) +
                ", modifiers=" + modifiers +
                ", returnableType=" + returnableType +
                ", args=" + Representable.repr(args) +
                ", body=" + body +
                '}';
    }
}
