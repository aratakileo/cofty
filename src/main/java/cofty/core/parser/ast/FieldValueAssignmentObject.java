package cofty.core.parser.ast;

import cofty.core.lexer.token.TypedToken;
import cofty.core.parser.ast.body.BodyResidentObject;
import cofty.core.parser.ast.value.ExpressionValueObject;
import cofty.core.parser.ast.value.complex.ComplexValueObject;
import cofty.core.parser.ast.value.complex.FieldAccessObject;
import org.jetbrains.annotations.NotNull;

import java.text.MessageFormat;
import java.util.List;

public final class FieldValueAssignmentObject implements BodyResidentObject, WithDiagnosticFailAnchor {
    public final ExpressionValueObject fieldView;
    public final ExpressionValueObject value;

    public FieldValueAssignmentObject(
            @NotNull ExpressionValueObject fieldView,
            @NotNull ExpressionValueObject value
    ) {
        this.fieldView = fieldView;
        this.value = value;
    }

    public @NotNull FieldAccessObject actualField() {
        if (fieldView instanceof FieldAccessObject fieldAccessObject)
            return fieldAccessObject;

        return (FieldAccessObject)((ComplexValueObject)fieldView).segments.getLast();
    }

    @Override
    public @NotNull List<TypedToken<?>> failTokensRange() {
        return List.of(fieldView.firstFailToken(), value.lastFailToken());
    }

    @Override
    public @NotNull String prettyString(@NotNull String offset, int increase) {
        return MessageFormat.format(
                "{2}assign `{0}` := {1}",
                fieldView.prettyString("", increase),
                value.prettyString("", increase),
                offset
        );
    }
}
