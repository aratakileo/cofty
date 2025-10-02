package cofty.core.parser.ast.statement;

import cofty.core.lexer.token.Keyword;
import cofty.type.Representable;
import cofty.type.exception.SyntaxError;
import cofty.util.Cast;
import cofty.core.parser.ast.AstObject;
import cofty.core.parser.ast.BodyObject;
import cofty.core.parser.node.ParserNode;
import cofty.core.parser.node.modifier.NodeModifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class IfStatementsObject implements AstObject {
    private final StatementObject ifStatement = new StatementObject(Keyword.IF);
    private final BodyObject elseBody = new BodyObject();

    private List<@NotNull StatementObject> elseIfStatements = null;

    private void setElseIfStatements(@NotNull List<AstObject> elseIfStatements) {
        for (final var statement: elseIfStatements)
            if (!(statement instanceof StatementObject)) throw new IllegalStateException();

        this.elseIfStatements = Cast.unsafe(elseIfStatements);
    }

    public @NotNull StatementObject ifStatement() {
        return ifStatement;
    }

    public @NotNull BodyObject elseBody() {
        return elseBody;
    }

    public @Nullable List<@NotNull StatementObject> elseIfStatements() {
        return elseIfStatements;
    }

    public @NotNull List<@NotNull StatementObject> elseIfStatementsOrThrow() {
        return Objects.requireNonNull(elseIfStatements);
    }

    @Override
    public @NotNull ParserNode parserNode() {
        var node = (ParserNode)null;

        (node = ifStatement.parserNode()).joinWith(
                ParserNode.repeatableQueueBuilder(
                        NodeModifier.builder()
                                .peek()
                                .astObjectsConsumer(this::setElseIfStatements)
                                .build()
                        ).add(() -> new StatementObject(Keyword.ELIF))
                        .build()
        ).joinWith(
                ParserNode.token(Keyword.ELSE, NodeModifier.peek())
                        .and(elseBody.multilineOrSingleLineSubbody(
                                new SyntaxError("expected a statement body description"),
                                NodeModifier.syntaxFail("expected an end of statement body description")
                        ), NodeModifier.builder().depended().general().build())
        );

        return node;
    }

    @Override
    public String toString() {
        return "IfStatementObject{" +
                "ifStatement=" + ifStatement +
                ", elseIfStatements=" + Representable.repr(elseIfStatements) +
                ", elseBody=" + elseBody +
                '}';
    }
}
