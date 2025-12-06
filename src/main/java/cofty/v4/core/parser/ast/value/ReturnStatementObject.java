package cofty.v4.core.parser.ast.value;

import cofty.v4.core.parser.ast.BodyResidentObject;
import org.jetbrains.annotations.Nullable;

public final class ReturnStatementObject implements BodyResidentObject {
    public final ExpressionValueObject value;

    public ReturnStatementObject(@Nullable ExpressionValueObject value) {
        this.value = value;
    }
}
