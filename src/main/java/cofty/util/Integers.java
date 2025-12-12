package cofty.util;

public final class Integers {
    private Integers() {}

    public static int digitsCount(int num) {
        return (int)(Math.log10(Math.max(1, Math.abs(num))) + 1);
    }
}
