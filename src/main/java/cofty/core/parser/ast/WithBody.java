package cofty.core.parser.ast;

import org.jetbrains.annotations.NotNull;

public interface WithBody {
    @NotNull BodyObject body();
}
