package cofty.v4.core.parser.ast;

import cofty.v4.core.parser.ast.value.ValueExpressionObject;
import cofty.v4.core.parser.ast.value.complex.ComplexValueObject;
import org.jetbrains.annotations.NotNull;

public final class FieldValueAssignmentObject implements BodyResidentObject {
    public final ComplexValueObject field;
    public final ValueExpressionObject value;

    public FieldValueAssignmentObject(
            @NotNull ComplexValueObject field,
            @NotNull ValueExpressionObject value
    ) {
        this.field = field;
        this.value = value;
    }
}
