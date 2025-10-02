package cofty.core.parser.ast.statement;

import cofty.core.lexer.token.Brackets;
import cofty.core.lexer.token.ITokenType;
import cofty.type.exception.SyntaxError;
import cofty.core.parser.ast.AstObject;
import cofty.core.parser.ast.BodyObject;
import cofty.core.parser.ast.WithBody;
import cofty.core.parser.ast.value.ValueExpressionObject;
import cofty.core.parser.node.ParserNode;
import cofty.core.parser.node.TokenNode;
import cofty.core.parser.node.modifier.NodeModifier;
import org.jetbrains.annotations.NotNull;

public class StatementObject implements AstObject, WithBody {
    private final ITokenType statementStartKeyword;
    private final ValueExpressionObject statement = new ValueExpressionObject();
    private final BodyObject body = new BodyObject();

    public StatementObject(@NotNull ITokenType statementStartKeyword) {
        this.statementStartKeyword = statementStartKeyword;
    }

    public @NotNull ITokenType statementStartKeyword() {
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

        (node = ParserNode.token(Brackets.ROUND_OPEN, NodeModifier.generalAndPreview()))
                .then(justStatementExpression(false))
                .thenToken(Brackets.ROUND_CLOSE, NodeModifier.syntaxFail("expected an end of statement description"));

        return node;
    }

    @Override
    public @NotNull ParserNode parserNode() {
        final var curveBracketCloseModifier = NodeModifier.syntaxFail("expected an end of statement body description");

        var node = (TokenNode)null;

        (node = ParserNode.token(statementStartKeyword, NodeModifier.generalAndPreview()))
                .thenAnyOf(
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
