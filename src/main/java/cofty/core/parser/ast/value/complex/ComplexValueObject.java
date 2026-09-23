package cofty.core.parser.ast.value.complex;

import cofty.core.lexer.token.TypedToken;
import cofty.core.parser.ast.value.ExpressionValueObject;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public final class ComplexValueObject implements ExpressionValueObject {
    public final List<ValueSegmentObject> segments;

    public ComplexValueObject(@NotNull List<ValueSegmentObject> segments) {
        this.segments = segments;
    }

    @Override
    public @NotNull List<TypedToken<?>> failTokensRange() {
        return List.of(segments.getFirst().firstFailToken(), segments.getLast().lastFailToken());
    }

    @Override
    public @NotNull String prettyString(@NotNull String offset, int increase) {
        return offset + segments.stream().map(segment -> segment.prettyString("", increase))
                .collect(Collectors.joining(" -> "));
    }
}
