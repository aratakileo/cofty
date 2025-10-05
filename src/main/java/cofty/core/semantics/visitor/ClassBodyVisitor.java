package cofty.core.semantics.visitor;

public class ClassBodyVisitor implements BodySubsidiaryVisitor {
    @Override
    public boolean allowStaticModifier() {
        return true;
    }
}
