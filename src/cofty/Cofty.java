package cofty;

import cofty.core.message.MessageHandler;
import cofty.core.parser.ParserContext;
import cofty.core.token.Lexer;
import cofty.type.Representable;
import cofty.type.TextContent;

public class Cofty {
    public static void main(String[] args) {
        final var text = TextContent.read("test.cft").unwrap();
        final var messages = new MessageHandler();
        final var lexer = new Lexer(text, messages);
        final var parsedTokens = lexer.parse();
        final var parser = new ParserContext(parsedTokens, text, messages);

        messages.print();

        System.out.println(Representable.repr(parsedTokens));

        parser.parse();

        messages.print();
    }
}
