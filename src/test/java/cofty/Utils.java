package cofty;

import cofty.core.lexer.Lexer;
import cofty.core.compiler.message.CompilationMessageHandler;
import cofty.core.parser.BodyParser;
import cofty.core.parser.ParseContext;
import cofty.core.semantics.ModuleContext;
import cofty.core.semantics.symbol.scope.RootScope;
import cofty.type.TextContent;
import org.jspecify.annotations.NonNull;

public final class Utils {
    private Utils() {}

    public static @NonNull ParseContext parseContextOf(@NonNull String sourceCode) {
        final var textContent = TextContent.ofInput(sourceCode);
        final var messages = new CompilationMessageHandler();
        final var tokens = new Lexer(textContent, messages).parse();

        return new ParseContext(tokens, textContent, messages);
    }

    public static @NonNull ModuleContext moduleContextOf(@NonNull String sourceCode) {
        final var parseContext = parseContextOf("""
                class int {}
                class null {}
                class float {}
                class bool {}
                class str {}
                """ + sourceCode);

        return ModuleContext.create(
                parseContext.text,
                parseContext.messages.handler,
                new RootScope(),
                BodyParser.MODULE_BODY.parse(parseContext).valueOrThrow()
        );
    }
}
