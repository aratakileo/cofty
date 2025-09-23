package cofty.core.lexer;

import cofty.core.lexer.token.*;
import cofty.core.message.MessageHandler;
import cofty.type.TextContent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class LexerTest {
    @Test
    void allValidTokensParse() {
        final var lexer = lexerOfText("1 2. s_3 let = . ( \n");
        final var tokens = lexer.parse();

        Assertions.assertEquals(tokens.size(), 8);
    }

    @Test
    void isIntegerParsedValid() {
        Assertions.assertEquals(firstToken("_1_4").type, TokenType.INT);
    }

    @Test
    void isDoubleParsedValid() {
        Assertions.assertEquals(firstToken("1.").type, TokenType.DOUBLE);
    }

    @Test
    void isIdParsedValid() {
        Assertions.assertEquals(firstToken("s_3").type, TokenType.ID);
    }

    @Test
    void isKeywordParsedValid() {
        Assertions.assertEquals(firstToken("fn").type, Keyword.FN);
    }

    @Test
    void isOperatorParsedValid() {
        Assertions.assertEquals(firstToken("=").type, Operator.ASSIGN);
    }

    @Test
    void isSeparatorParsedValid() {
        Assertions.assertEquals(firstToken("->").type, Separator.ARROW);
    }

    @Test
    void isBracketParsedValid() {
        Assertions.assertEquals(firstToken("}").type, Brackets.CURVE_RIGHT);
    }

    @Test
    void isNewLineParsedValid() {
        final var lexer = lexerOfText("  \n     \n  ");
        final var tokens = lexer.parse();

        Assertions.assertEquals(tokens.size(), 1);
        Assertions.assertEquals(tokens.get(0).type, TokenType.NEWLINE);
    }

    private Lexer lexerOfText(String text) {
        return new Lexer(TextContent.ofInput(text), new MessageHandler());
    }

    private Token firstToken(String token) {
        final var lexer = lexerOfText(token);
        final var tokens = lexer.parse();

        if (tokens.size() != 1) throw new IllegalStateException();

        return tokens.get(0);
    }
}