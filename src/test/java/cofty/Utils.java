package cofty;

import cofty.core.lexer.Lexer;
import cofty.core.message.MessageHandler;
import cofty.core.parser.ParseContext;
import cofty.type.TextContent;

public final class Utils {
    private Utils() {}

    public static ParseContext parseContextOf(String text) {
        final var textContent = TextContent.ofInput(text);
        final var messages = new MessageHandler();
        final var tokens = new Lexer(textContent, messages).parse();

        return new ParseContext(tokens, textContent, messages);
    }
}
