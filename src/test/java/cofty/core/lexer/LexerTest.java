package cofty.core.lexer;

import cofty.core.lexer.token.*;
import cofty.core.lexer.token.type.*;
import cofty.core.lexer.token.type.operator.*;
import cofty.v4.core.compiler.message.CompilationMessageHandler;
import cofty.type.TextContent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class LexerTest {
    @Test
    void validAllTokensParsed() {
        final var lexer = lexerOfText("1 2. s_3 let static = . ( \n 'Hello world!\\n'");
        final var tokens = lexer.parse();

        Assertions.assertEquals(10, tokens.size(), "there must be 10 tokens");
    }

    @Test
    void validKeywordAndIdStartsWithKeyword() {
        final var lexer = lexerOfText("var variable");
        final var tokens = lexer.parse();

        Assertions.assertEquals(2, tokens.size(), "there must be two tokens (not three)");
        Assertions.assertEquals(Keyword.VAR, tokens.get(0).type, "the first token must be a keyword token");
        Assertions.assertEquals(Simple.WORD, tokens.get(1).type, "the second token must be a word token");
    }

    @Test
    void validIntegerParsed() {
        Assertions.assertEquals(Simple.INT, firstToken("_1_4").type);
        Assertions.assertEquals(Simple.INT, firstToken("14").type);
        Assertions.assertEquals(Simple.INT, firstToken("0xff").type);
        Assertions.assertEquals(Simple.INT, firstToken("_0_X_ff_").type);
    }

    @Test
    void validDoubleNumberParsed() {
        Assertions.assertEquals(Simple.DOUBLE, firstToken("1.").type);
        Assertions.assertEquals(Simple.DOUBLE, firstToken("3.432").type);
        Assertions.assertEquals(Simple.DOUBLE, firstToken("_4_._0__").type);
        Assertions.assertEquals(Simple.DOUBLE, firstToken("5d").type);
    }

    @Test
    void validStringValueParsed() {
        Assertions.assertEquals(Simple.STR, firstToken("'Hello world!\\n'").type);
        Assertions.assertEquals(Simple.STR, firstToken("\"Hello world!\\n\"").type);
    }

    @Test
    void validWordParsed() {
        Assertions.assertEquals(Simple.WORD, firstToken("s_3").type);
        Assertions.assertEquals(Simple.WORD, firstToken("____s").type);
    }

    @Test
    void validKeywordParsed() {
        Assertions.assertEquals(Keyword.FUN, firstToken("fun").type);
        Assertions.assertEquals(Modifier.PUBLIC, firstToken("public").type);
    }

    @Test
    void validOperatorParsed() {
        Assertions.assertEquals(Assign.ASSIGN, firstToken("=").type);
        Assertions.assertEquals(Separator.ARROW, firstToken("->").type);
        Assertions.assertEquals(Separator.COLON, firstToken(":").type);
        Assertions.assertEquals(Bracket.CURVE_CLOSE, firstToken("}").type);
        Assertions.assertEquals(ContextSensitive.PLUS, firstToken("+").type);
        Assertions.assertEquals(Unary.NOT, firstToken("not").type);
        Assertions.assertEquals(Binary.IS, firstToken("is").type);
    }

    @Test
    void validNewLinesAsSingleToken() {
        final var lexer = lexerOfText("are  \n     \n  ");
        final var tokens = lexer.parse();

        Assertions.assertEquals(
                2,
                tokens.size(),
                "the result of two new lines and spaces must be one other token and one new line token");

        Assertions.assertEquals(
                Simple.NEWLINE,
                tokens.get(1).type,
                "the resulted token type must be " + Simple.NEWLINE.toReprString()
        );
    }

    @Test
    void validNewLineAsFirstTokenInQueueIsRemoved() {
        final var lexer = lexerOfText("\n     \n  are");
        final var tokens = lexer.parse();

        Assertions.assertEquals(
                1,
                tokens.size(),
                "the result of expression must be one word token");

        Assertions.assertEquals(
                Simple.WORD,
                tokens.getFirst().type,
                "the resulted token type must be " + Simple.NEWLINE.toReprString()
        );
    }

    private Lexer lexerOfText(String text) {
        return new Lexer(TextContent.ofInput(text), new CompilationMessageHandler());
    }

    private TypedToken<?> firstToken(String token) {
        final var lexer = lexerOfText(token);
        final var tokens = lexer.parse();

        if (tokens.size() != 1) throw new IllegalStateException();

        return tokens.getFirst();
    }
}