package cofty.core.semantics.visitor;

@Deprecated
public class ClassBodyVisitor implements BodySubsidiaryVisitor {
    @Override
    public boolean allowStaticModifier() {
        return true;
    }
}
