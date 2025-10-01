package cofty.v3.core.semantics.visitor;

public class ClassVisitor implements BodySubsidiaryVisitor {
    @Override
    public boolean allowSubBodies() {
        return false;
    }
}
