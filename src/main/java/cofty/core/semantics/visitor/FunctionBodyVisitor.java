package cofty.core.semantics.visitor;

@Deprecated
public class FunctionBodyVisitor extends SubBodyVisitor {
    @Override
    public boolean allowReturnStatement() {
        return true;
    }
}
