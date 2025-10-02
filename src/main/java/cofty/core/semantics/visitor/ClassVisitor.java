package cofty.core.semantics.visitor;

public class ClassVisitor implements BodySubsidiaryVisitor {
    @Override
    public boolean allowSubBodies() {
        return false;
    }
}
