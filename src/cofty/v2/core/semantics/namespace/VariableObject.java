package cofty.v2.core.semantics.namespace;

import cofty.v2.core.semantics.Path;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VariableObject extends NamespaceObject {
    public final String name;
    public final NamespaceValue value;
    public final Path valueType;

    public VariableObject(@NotNull Path path, @NotNull Path valueType, @Nullable NamespaceValue value) {
        super(path);

        this.name = path.segments.getLast();
        this.value = value;
        this.valueType = valueType;
    }
}
