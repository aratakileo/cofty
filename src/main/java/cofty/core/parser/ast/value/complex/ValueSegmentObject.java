package cofty.core.parser.ast.value.complex;

import cofty.core.compiler.message.CompilationMessageRepresentable;
import cofty.core.parser.ast.WithFailMessageAnchor;
import cofty.core.parser.ast.value.ExpressionValueObject;

public interface ValueSegmentObject extends ExpressionValueObject, CompilationMessageRepresentable, WithFailMessageAnchor {
}
