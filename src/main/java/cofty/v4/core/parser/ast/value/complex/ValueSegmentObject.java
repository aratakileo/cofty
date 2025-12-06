package cofty.v4.core.parser.ast.value.complex;

import cofty.v4.core.compiler.message.CompilationMessageRepresentable;
import cofty.v4.core.parser.ast.WithFailMessageAnchor;
import cofty.v4.core.parser.ast.value.ExpressionValueObject;

public interface ValueSegmentObject extends ExpressionValueObject, CompilationMessageRepresentable, WithFailMessageAnchor {
}
