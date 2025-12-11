package cofty.core.parser.ast.body;

import cofty.core.parser.ast.WithBody;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class BodyObject implements BodyResidentObject, WithBody {
    public final List<BodyResidentObject> residents;
    public final boolean finishedWithReturnStatement;

    protected BodyObject(@NotNull List<BodyResidentObject> residents, boolean finishedWithReturnStatement) {
        this.residents = residents;
        this.finishedWithReturnStatement = finishedWithReturnStatement;
    }

    @Override
    public @NotNull List<BodyResidentObject> residents() {
        return residents;
    }
}
