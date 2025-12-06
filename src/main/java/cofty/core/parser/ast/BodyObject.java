package cofty.core.parser.ast;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class BodyObject implements BodyResidentObject {
    public final List<BodyResidentObject> residents;
    public final boolean finishedWithReturnStatement;

    public BodyObject(@NotNull List<BodyResidentObject> residents, boolean finishedWithReturnStatement) {
        this.residents = residents;
        this.finishedWithReturnStatement = finishedWithReturnStatement;
    }
}
