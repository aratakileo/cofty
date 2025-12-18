package cofty.core.semantics.symbol;

public interface CompletedSymbol extends Symbol {
    boolean isCompletedInsideOfMainCycle();

    void markAsCompletedInsideOfMainCycle();
}
