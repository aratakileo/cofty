package cofty.v2.core.ast.object;

import cofty.v2.core.ast.AstObject;
import cofty.v2.core.parse.AstParser;
import cofty.core.lexer.token.Token;
import cofty.core.lexer.token.TokenType;

import java.util.Optional;

public class ValueAst implements AstObject {
    public final static AstParser<ValueAst> PARSER = context -> {
        final var result = new ValueAst();

        context.startTransaction()
                .match(AstParser.any(TokenType.INT, TokenType.DOUBLE), token -> {
                    result.value = token;
                    result.valueType = switch (token.type) {
                        case TokenType.INT -> ValueType.INT;
                        case TokenType.DOUBLE -> ValueType.DOUBLE;
                        default -> throw new IllegalStateException();
                    };
                })
                .syntaxErrorOnFail_v2("expected value")
                .finishTransaction();

        return context.isFailed_v2() ? Optional.empty() : Optional.of(result);
    };

    public Token value;
    public ValueType valueType;

    @Override
    public String toString() {
        return "ValueAst{" +
                "value='" + value + '\'' +
                ", valueType=" + valueType +
                '}';
    }

    public enum ValueType {
        INT,
        DOUBLE
    }
}
