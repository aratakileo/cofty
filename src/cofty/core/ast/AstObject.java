package cofty.core.ast;

public interface AstObject extends AstValue {
    @Override
    default boolean isAstObject() {
        return true;
    }
}
