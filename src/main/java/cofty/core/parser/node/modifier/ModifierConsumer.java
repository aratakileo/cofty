package cofty.core.parser.node.modifier;

import cofty.core.lexer.token.TypedToken;
import cofty.core.parser.ast.AstObject;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Deprecated
public interface ModifierConsumer {
    default void consume(@NotNull TypedToken<?> token) {
        throw new RuntimeException("unacceptable type of consumption");
    }

    default void consume(@NotNull AstObject astObject) {
        throw new RuntimeException("unacceptable type of consumption");
    }

    default void consume(@NotNull List<AstObject> astObjects) {
        throw new RuntimeException("unacceptable type of consumption");
    }
}
