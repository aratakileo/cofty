package cofty.v2.core.ast;

public interface AstValue {
    default boolean isAstObject() {
        return false;
    }
}
