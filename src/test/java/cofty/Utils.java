package cofty;

import cofty.core.parser.BodyParser;
import cofty.core.semantics.ModuleContext;
import cofty.core.semantics.symbol.scope.RootScope;
import org.jspecify.annotations.NonNull;

public final class Utils {
    private Utils() {}

    public static @NonNull ModuleContext moduleContextOf(@NonNull String sourceCode) {
        final var parseContext = ParseResultAssert.parse(
                """
                class int {}
                class null {}
                class float {}
                class bool {}
                class str {}
                """ + sourceCode,
                BodyParser.MODULE_BODY
        ).ok().hasNoDiagnosticMessages();

        return ModuleContext.create(
                parseContext.context.text,
                parseContext.context.messages.engine,
                new RootScope(),
                parseContext.value()
        );
    }
}
