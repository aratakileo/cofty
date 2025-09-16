package cofty.v2.core.semantics.namespace;

import cofty.v2.core.semantics.Path;
import org.jetbrains.annotations.NotNull;

public abstract class NamespaceObject {
    public final Path path;

    public NamespaceObject(@NotNull Path path) {
        this.path = path;
    }
}
