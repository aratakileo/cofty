package cofty.core.parser.ast.body;

import cofty.core.parser.ast.DeclarationObject;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ModuleBodyObject extends BodyObject implements DeclarationObject {
    public final String name;

    public ModuleBodyObject(@NotNull String name, @NotNull List<BodyResidentObject> residents) {
        super(residents, false);
        this.name = name;
    }

    @Override
    public @NotNull String prettyString(@NotNull String offset, int increase) {
        return prettyString(offset, increase, false);
    }
}
