package cofty.v3.core.parser.node.modifier;

import cofty.core.token.Token;
import org.jetbrains.annotations.NotNull;

public interface ModifierAction {
    void applyToken(@NotNull Token token);
}
