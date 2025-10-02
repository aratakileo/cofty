package cofty.core.semantics.visitor;

public class FunctionBodyVisitor extends SubBodyVisitor {
    @Override
    public boolean allowReturnStatement() {
        return true;
    }
}
