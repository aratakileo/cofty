package cofty.v2.core.parse;

import cofty.core.parse.ParseContext;
import cofty.v2.core.ast.AstValue;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface AstParseEntry<R extends AstValue> extends AstParser<R>, AstPeeker {
    static <R extends AstValue> @NotNull AstParseEntry<R> bind(@NotNull AstPeeker peeker, @NotNull AstParser<R> parser) {
        return new AstParseEntry<>() {
            @Override
            public @NotNull Optional<R> parse(@NotNull ParseContext context) {
                return parser.parse(context);
            }

            @Override
            public boolean safePeek(@NotNull ParseContext context) {
                return peeker.safePeek(context);
            }
        };
    }
}
