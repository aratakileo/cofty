package cofty.core.parser.ast.body;

import cofty.core.parser.ast.WithBody;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

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

    protected @NotNull String prettyString(@NotNull String offset, int increase, boolean firstLevelOffset) {
        return residents.stream()
                .map(resident -> resident.prettyString(
                        firstLevelOffset ? offset.length() + increase : 0,
                        increase
                )).collect(Collectors.joining("\n"));
    }

    @Override
    public @NotNull String prettyString(@NotNull String offset, int increase) {
        return prettyString(offset, increase, true);
    }
}
