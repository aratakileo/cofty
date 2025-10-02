package cofty.core.lexer;

import cofty.core.lexer.token.*;
import cofty.core.lexer.token.type.*;
import cofty.core.message.MessageHandler;
import cofty.type.TextContent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class LexerTest {
    @Test
    void allValidTokensParse() {
        final var lexer = lexerOfText("1 2. s_3 let = . ( \n 'Hello world!\\n'");
        final var tokens = lexer.parse();

        Assertions.assertEquals(9, tokens.size());
    }

    @Test
    void validKeywordAndIdStartsWithKeyword() {
        final var lexer = lexerOfText("var variable");
        final var tokens = lexer.parse();

        Assertions.assertEquals(2, tokens.size());
    }

    @Test
    void isIntegerParsedValid() {
        Assertions.assertEquals(Simple.INT, firstToken("_1_4").type);
    }

    @Test
    void isDoubleParsedValid() {
        Assertions.assertEquals(Simple.DOUBLE, firstToken("1.").type);
    }

    @Test
    void isStrParsedValid() {
        Assertions.assertEquals(Simple.STR, firstToken("'Hello world!\\n'").type);
    }

    @Test
    void isIdParsedValid() {
        Assertions.assertEquals(Simple.WORD, firstToken("s_3").type);
    }

    @Test
    void isKeywordParsedValid() {
        Assertions.assertEquals(Keyword.FUN, firstToken("fun").type);
    }

    @Test
    void isOperatorParsedValid() {
        Assertions.assertEquals(Operator.ASSIGN, firstToken("=").type);
    }

    @Test
    void isSeparatorParsedValid() {
        Assertions.assertEquals(Separator.ARROW, firstToken("->").type);
    }

    @Test
    void isBracketParsedValid() {
        Assertions.assertEquals(Brackets.CURVE_CLOSE, firstToken("}").type);
    }

    @Test
    void isNewLineParsedValid() {
        final var lexer = lexerOfText("  \n     \n  ");
        final var tokens = lexer.parse();

        Assertions.assertEquals(
                1,
                tokens.size(),
                "the result of two new lines and spaces should be one new line token");

        Assertions.assertEquals(
                Simple.NEWLINE,
                tokens.get(0).type,
                "the resulted token type should be " + Simple.NEWLINE.toReprString()
        );
    }

    private Lexer lexerOfText(String text) {
        return new Lexer(TextContent.ofInput(text), new MessageHandler());
    }

    private TypedToken<?> firstToken(String token) {
        final var lexer = lexerOfText(token);
        final var tokens = lexer.parse();

        if (tokens.size() != 1) throw new IllegalStateException();

        return tokens.getFirst();
    }
}