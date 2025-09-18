package cofty.v2.core.ast.object;

import cofty.v2.core.ast.AstObject;
import cofty.v2.core.parse.AstParseEntry;
import cofty.v2.core.parse.AstParser;
import cofty.v2.core.parse.AstPeeker;
import cofty.core.lexer.token.*;

import java.util.Optional;

public class InitVarAst implements AstObject {
    public final static AstPeeker PEEKER = context -> context.currentOrThrow().type.equals(Keyword.LET);

    public final static AstParser<InitVarAst> PARSER = context -> {
        final var result = new InitVarAst();

        context.startTransaction()
                .match(Keyword.LET)
                .syntaxErrorOnFail_v2("expected `let` keyword")
                .startTransaction()
                .match(Keyword.MUT, ignore -> result.mutable = true)
                .finishTransaction(true)
                .match(TokenType.ID, idToken -> result.name = idToken)
                .startTransaction()
                .match(Separator.COLON)
                .match(TokenType.ID, idToken -> result.type = idToken)
                .syntaxErrorOnFail_v2("expected variable type")
                .finishTransaction(true)
                .startTransaction()
                .match(Operator.ASSIGN)
                .match(ValueAst.PARSER, valueAst -> result.value = valueAst)
                .finishTransaction(true)
                .finishTransaction();

        return context.isFailed_v2() ? Optional.empty() : Optional.of(result);
    };

    public final static AstParseEntry<InitVarAst> PARSE_ENTRY = AstParseEntry.bind(PEEKER, PARSER);

    public Token name, type = null;
    public boolean mutable = false;
    public ValueAst value = null;

    @Override
    public String toString() {
        return "InitVarAst{" +
                "name=" + name +
                ", type=" + type +
                ", mutable=" + mutable +
                ", value=" + value +
                '}';
    }
}
