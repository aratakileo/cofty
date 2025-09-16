package cofty.v2.core.ast;

public interface AstObject extends AstValue {
    @Override
    default boolean isAstObject() {
        return true;
    }
}
