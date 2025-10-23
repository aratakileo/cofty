package cofty.core.lexer;

import cofty.core.lexer.token.*;
import cofty.core.lexer.token.type.*;
import cofty.core.lexer.token.type.operator.Assign;
import cofty.core.lexer.token.type.operator.Bracket;
import cofty.core.lexer.token.type.operator.Separator;
import cofty.core.message.MessageHandler;
import cofty.type.TextContent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class LexerTest {
    @Test
    void validAllTokensParsed() {
        final var lexer = lexerOfText("1 2. s_3 let static = . ( \n 'Hello world!\\n'");
        final var tokens = lexer.parse();

        Assertions.assertEquals(10, tokens.size(), "there should be 10 tokens");
    }

    @Test
    void validKeywordAndIdStartsWithKeyword() {
        final var lexer = lexerOfText("var variable");
        final var tokens = lexer.parse();

        Assertions.assertEquals(2, tokens.size(), "there should be two tokens (not three)");
        Assertions.assertEquals(Keyword.VAR, tokens.get(0).type, "the first token should be a keyword token");
        Assertions.assertEquals(Simple.WORD, tokens.get(1).type, "the second token should be a word token");
    }

    @Test
    void validIntegerParsed() {
        Assertions.assertEquals(Simple.INT, firstToken("_1_4").type);
    }

    @Test
    void validDoubleNumberParsed() {
        Assertions.assertEquals(Simple.DOUBLE, firstToken("1.").type);
    }

    @Test
    void validStringValueParsed() {
        Assertions.assertEquals(Simple.STR, firstToken("'Hello world!\\n'").type);
    }

    @Test
    void validWordParsed() {
        Assertions.assertEquals(Simple.WORD, firstToken("s_3").type);
    }

    @Test
    void validKeywordParsed() {
        Assertions.assertEquals(Keyword.FUN, firstToken("fun").type);
    }

    @Test
    void validModifierKeywordParsed() {
        Assertions.assertEquals(Modifier.STATIC, firstToken("static").type);
    }

    @Test
    void validOperatorParsed() {
        Assertions.assertEquals(Assign.ASSIGN, firstToken("=").type);
    }

    @Test
    void validSeparatorOperatorParsed() {
        Assertions.assertEquals(Separator.ARROW, firstToken("->").type);
    }

    @Test
    void validBracketParsed() {
        Assertions.assertEquals(Bracket.CURVE_CLOSE, firstToken("}").type);
    }

    @Test
    void validNewLinesAsSingleToken() {
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