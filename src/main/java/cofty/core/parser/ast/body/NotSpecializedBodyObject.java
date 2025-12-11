package cofty.core.parser.ast.body;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class NotSpecializedBodyObject extends BodyObject {
    public NotSpecializedBodyObject(@NotNull List<BodyResidentObject> residents, boolean finishedWithReturnStatement) {
        super(residents, finishedWithReturnStatement);
    }
}
