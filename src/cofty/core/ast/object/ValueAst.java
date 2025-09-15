package cofty.core.ast.object;

import cofty.core.ast.AstObject;
import cofty.core.parse.AstParser;
import cofty.core.token.Token;
import cofty.core.token.TokenType;

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

        return context.isFailed() ? Optional.empty() : Optional.of(result);
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
