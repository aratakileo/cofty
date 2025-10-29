package cofty;

import cofty.type.TextContent;
import cofty.v4.core.compiler.Compiler;

public class Cofty {
    public static void main(String[] args) {
        final var compiler = new Compiler(TextContent.read("test.cft").unwrap());

        compiler.compile();
        compiler.resultLogger.print();
    }
}
