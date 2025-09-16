package cofty.v3.parser.node.flag;

import cofty.core.token.Token;
import org.jetbrains.annotations.NotNull;

public interface SucceedApplier {
    void apply(@NotNull Token token);
}
