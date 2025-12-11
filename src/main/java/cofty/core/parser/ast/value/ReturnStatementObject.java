package cofty.core.parser.ast.value;

import cofty.core.parser.ast.body.BodyResidentObject;
import org.jetbrains.annotations.Nullable;

public final class ReturnStatementObject implements BodyResidentObject {
    public final ExpressionValueObject value;

    public ReturnStatementObject(@Nullable ExpressionValueObject value) {
        this.value = value;
    }
}
