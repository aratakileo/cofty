package cofty.v2.core.semantics.namespace;

import cofty.v2.core.semantics.Path;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class BodyContainerObject extends NamespaceObject {
    public final ArrayList<? extends NamespaceObject> body = new ArrayList<>();

    public BodyContainerObject(@NotNull Path path) {
        super(path);
    }
}
