package cofty.v3.core.parser.node.modifier;

import cofty.core.lexer.token.Token;
import cofty.v3.core.parser.ast.AstObject;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ModifierAction {
    default void apply(@NotNull Token token) {
        throw new RuntimeException("unacceptable type of consumption");
    }

    default void apply(@NotNull AstObject astObject) {
        throw new RuntimeException("unacceptable type of consumption");
    }

    default void apply(@NotNull List<AstObject> astObjects) {
        throw new RuntimeException("unacceptable type of consumption");
    }
}
