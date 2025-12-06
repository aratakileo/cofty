package cofty.v4.core.parser.ast;

import cofty.v4.core.parser.ast.value.ExpressionValueObject;
import org.jetbrains.annotations.NotNull;

public final class FieldValueAssignmentObject implements BodyResidentObject {
    public final ExpressionValueObject field;
    public final ExpressionValueObject value;

    public FieldValueAssignmentObject(
            @NotNull ExpressionValueObject field,
            @NotNull ExpressionValueObject value
    ) {
        this.field = field;
        this.value = value;
    }
}
