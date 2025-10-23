package cofty;

import cofty.core.Compiler;
import cofty.type.TextContent;

public class Cofty {
    public static void main(String[] args) {
        final var compiler = new Compiler(TextContent.read("test.cft").unwrap());

        compiler.compile();
        compiler.resultLogger.print();
    }
}
