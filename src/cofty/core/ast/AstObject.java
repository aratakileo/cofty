package cofty.core.ast;

public abstract class AstObject<T extends IAstObject<?>> implements IAstObject<T> {
    private boolean frozen = false;

    @Override
    public boolean frozen() {
        return frozen;
    }

    @Override
    public void freeze() {
        frozen = true;
    }
}
