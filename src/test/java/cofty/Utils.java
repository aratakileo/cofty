package cofty;

import cofty.core.lexer.Lexer;
import cofty.v4.core.compiler.message.CompilationMessageHandler;
import cofty.v4.core.parser.ParseContext;
import cofty.type.TextContent;

public final class Utils {
    private Utils() {}

    public static ParseContext parseContextOf(String text) {
        final var textContent = TextContent.ofInput(text);
        final var messages = new CompilationMessageHandler();
        final var tokens = new Lexer(textContent, messages).parse();

        return new ParseContext(tokens, textContent, messages);
    }
}
