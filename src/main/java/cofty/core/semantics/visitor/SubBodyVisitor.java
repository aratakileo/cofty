package cofty.core.semantics.visitor;

@Deprecated
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
