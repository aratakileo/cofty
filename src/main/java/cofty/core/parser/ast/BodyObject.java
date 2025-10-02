package cofty.core.parser.ast;

import cofty.core.lexer.token.type.Brackets;
import cofty.core.lexer.token.type.TokenType;
import cofty.core.lexer.token.type.Simple;
import cofty.type.Representable;
import cofty.core.parser.ast.func.CallFuncObject;
import cofty.core.parser.ast.func.FuncDeclarationObject;
import cofty.core.parser.ast.func.ReturnExpressionObject;
import cofty.core.parser.ast.statement.IfStatementsObject;
import cofty.core.parser.node.ParserNode;
import cofty.core.parser.node.TokenNode;
import cofty.core.parser.node.modifier.NodeModifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class BodyObject implements AstObject {
    private List<AstObject> objects = null;

    private void setObjects(@NotNull List<AstObject> objects) {
        this.objects = objects;
    }

    public @Nullable List<AstObject> objects() {
        return objects;
    }

    public @NotNull List<AstObject> objectsOrThrow() {
        return Objects.requireNonNull(objects);
    }

    @Override
    public @NotNull ParserNode parserNode() {
        return parserNode(null, null);
    }

    public @NotNull ParserNode parserNode(@NotNull TokenType stopper) {
        return parserNode(stopper, null);
    }

    public @NotNull ParserNode parserNode(@Nullable TokenType stopper, @Nullable Exception emptyBodyException) {
        final var builder = ParserNode.repeatableQueueBuilder(
                NodeModifier.builder()
                        .preview()
                        .astObjectsConsumer(this::setObjects)
                        .syntaxFail("invalid syntax")
                        .build()
        ).setSeparator(ParserNode.token(
                Simple.NEWLINE,
                NodeModifier.builder()
                        .syntaxFail("expected the new expression would start on a new line")
                        .preview()
                        .build()
        )).setEmptyBodyException(emptyBodyException)
        .add(
                () -> new VarDeclarationObject(false),
                SetVarValueObject::new,
                FuncDeclarationObject::new,
                CallFuncObject::new,
                ReturnExpressionObject::new,
                IfStatementsObject::new,
                ClassObject::new
        );

        if (stopper != null)
            builder.setStopper(ParserNode.token(stopper, NodeModifier.generalAndPreview()));

        return builder.build();
    }

    public @NotNull ParserNode singleLineSubbody(@Nullable Exception emptyBodyException) {
        return parserNode(Simple.NEWLINE, emptyBodyException)
                .andToken(Simple.NEWLINE, NodeModifier.peek());
    }

    public @NotNull ParserNode multilineSubbody(
            @NotNull NodeModifier curveBracketOpenModifier,
            @NotNull NodeModifier curveBracketCloseModifier
    ) {
        var node = (TokenNode)null;

        (node = ParserNode.token(Brackets.CURVE_OPEN, curveBracketOpenModifier))
                .then(parserNode(Brackets.CURVE_CLOSE))
                .thenToken(Brackets.CURVE_CLOSE, curveBracketCloseModifier);

        return node;
    }

    public @NotNull ParserNode multilineOrSingleLineSubbody(
            @Nullable Exception emptySinleLineBodyException,
            @NotNull NodeModifier curveBracketCloseModifier
    ) {
        return ParserNode.anyOf(
                NodeModifier.generalAndPreview(),
                multilineSubbody(NodeModifier.generalAndPreview(), curveBracketCloseModifier),
                singleLineSubbody(emptySinleLineBodyException)
        );
    }

    @Override
    public String toString() {
        return "BodyObject{" +
                "objects=" + Representable.repr(objects) +
                '}';
    }
}
