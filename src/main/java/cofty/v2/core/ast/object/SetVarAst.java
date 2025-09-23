package cofty.v2.core.ast.object;

import cofty.v2.core.ast.AstObject;
import cofty.v2.core.parse.AstParseEntry;
import cofty.v2.core.parse.AstParser;
import cofty.v2.core.parse.AstPeeker;
import cofty.core.lexer.token.Operator;
import cofty.core.lexer.token.Token;
import cofty.core.lexer.token.TokenType;

import java.util.Optional;

public class SetVarAst implements AstObject {
    public final static AstPeeker PEEKER = context -> context.currentOrThrow().type.equals(TokenType.ID) && context.peek(1, token -> token.type.equals(Operator.ASSIGN));

    public final static AstParser<SetVarAst> PARSER = context -> {
        final var result = new SetVarAst();

        context.startTransaction()
                .match(TokenType.ID, idToken -> result.name = idToken)
                .syntaxErrorOnFail_v2("expected var id")
                .match(Operator.ASSIGN)
                .syntaxErrorOnFail_v2("expected assign")
                .match(ValueAst.PARSER, valueAst -> result.value = valueAst)
                .finishTransaction();

        return context.isFailed_v2() ? Optional.empty() : Optional.of(result);
    };

    public final static AstParseEntry<SetVarAst> PARSE_ENTRY = AstParseEntry.bind(PEEKER, PARSER);

    public Token name;
    public ValueAst value = null;

    @Override
    public String toString() {
        return "SetVarAst{" +
                "name=" + name +
                ", value=" + value +
                '}';
    }
}
