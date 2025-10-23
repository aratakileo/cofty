package cofty.core.parser.ast.statement;

import cofty.core.lexer.token.TypedToken;
import cofty.core.lexer.token.type.operator.Bracket;
import cofty.core.lexer.token.type.TokenType;
import cofty.core.parser.ast.WithAnchor;
import cofty.type.exception.SyntaxError;
import cofty.core.parser.ast.AstObject;
import cofty.core.parser.ast.BodyObject;
import cofty.core.parser.ast.WithBody;
import cofty.core.parser.ast.value.expr.ValueExpressionObject;
import cofty.core.parser.node.ParserNode;
import cofty.core.parser.node.TokenNode;
import cofty.core.parser.node.modifier.NodeModifier;
import org.jetbrains.annotations.NotNull;

public class StatementObject<T extends TokenType> implements AstObject, WithBody, WithAnchor<T> {
    private final T statementStartKeyword;
    private final ValueExpressionObject statement = new ValueExpressionObject();
    private final BodyObject body = new BodyObject();

    private TypedToken<T> anchor = null;

    public StatementObject(@NotNull T statementStartKeyword) {
        this.statementStartKeyword = statementStartKeyword;
    }

    private void setAnchor(@NotNull TypedToken<?> anchor) {
        this.anchor = anchor.strictAs();
    }

    @Override
    public @NotNull TypedToken<T> anchor() {
        return anchor;
    }

    public @NotNull T statementStartKeyword() {
        return statementStartKeyword;
    }

    public @NotNull ValueExpressionObject statement() {
        return statement;
    }

    public @NotNull BodyObject body() {
        return body;
    }

    private @NotNull ParserNode justStatementExpression(boolean isOutOfParenthesis) {
        return ParserNode.contain(
                statement.parserNode(),
                isOutOfParenthesis ? NodeModifier.generalAndPreview()
                        : NodeModifier.syntaxFail("expected a statement expression")
        );
    }

    private @NotNull ParserNode statementExpressionWithParenthesis() {
        var node = (TokenNode)null;

        (node = ParserNode.token(Bracket.ROUND_OPEN, NodeModifier.generalAndPreview()))
                .then(justStatementExpression(false))
                .thenToken(Bracket.ROUND_CLOSE, NodeModifier.syntaxFail("expected an end of statement description"));

        return node;
    }

    @Override
    public @NotNull ParserNode parserNode() {
        final var curveBracketCloseModifier = NodeModifier.syntaxFail("expected an end of statement body description");

        var node = (TokenNode)null;

        (node = ParserNode.token(
                statementStartKeyword,
                NodeModifier.builder().general().preview().tokenConsumer(this::setAnchor).build()
        )).thenAnyOf(
                        NodeModifier.general(),
                        statementExpressionWithParenthesis().joinWith(body.multilineOrSingleLineSubbody(
                                new SyntaxError("expected a statement body description"),
                                curveBracketCloseModifier
                        )),
                        justStatementExpression(true).joinWith(body.multilineSubbody(
                                NodeModifier.syntaxFail("expected a statement body starts with `{`"),
                                curveBracketCloseModifier
                        ))
                );

        return node;
    }

    @Override
    public String toString() {
        return "StatementObject{" +
                "statementStartKeyword=" + statementStartKeyword +
                ", statement=" + statement +
                ", body=" + body +
                '}';
    }
}
