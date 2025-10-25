package cofty.v4.core.parser.ast;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class BodyObject implements BodyResidentObject {
    public final List<BodyResidentObject> residents;

    public BodyObject(@NotNull List<BodyResidentObject> residents) {
        this.residents = residents;
    }
}
