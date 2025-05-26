package cofty.core.ast;

public interface AstValue {
    default boolean isAstObject() {
        return false;
    }
}
