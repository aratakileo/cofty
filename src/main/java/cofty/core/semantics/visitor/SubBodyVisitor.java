package cofty.core.semantics.visitor;

public class SubBodyVisitor implements BodySubsidiaryVisitor {
    @Override
    public boolean allowModifiers() {
        return false;
    }

    @Override
    public boolean allowFunctionsOrClasses() {
        return false;
    }
}
