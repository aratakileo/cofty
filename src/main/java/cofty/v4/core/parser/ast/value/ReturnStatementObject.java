package cofty.v4.core.parser.ast.value;

import cofty.v4.core.parser.ast.BodyResidentObject;
import org.jetbrains.annotations.Nullable;

public final class ReturnStatementObject implements BodyResidentObject {
    public final ValueExpressionObject value;

    public ReturnStatementObject(@Nullable ValueExpressionObject value) {
        this.value = value;
    }
}
