package cofty.core.parser.ast;

import cofty.core.parser.ast.body.BodyResidentObject;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface WithBody {
    @NotNull List<BodyResidentObject> residents();
}
