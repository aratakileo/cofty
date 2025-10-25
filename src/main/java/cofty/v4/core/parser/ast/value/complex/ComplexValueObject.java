package cofty.v4.core.parser.ast.value.complex;

import cofty.v4.core.parser.ast.value.ExpressionValue;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class ComplexValueObject implements ExpressionValue {
    public final List<ValueSegmentObject> segments;

    public ComplexValueObject(@NotNull List<ValueSegmentObject> segments) {
        this.segments = segments;
    }
}
