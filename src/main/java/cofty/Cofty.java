package cofty;

import cofty.type.TextContent;
import cofty.core.compiler.Compiler;

public class Cofty {
    public static void main(String[] args) {
        final var compiler = new Compiler(TextContent.read("test.cft").unwrap());

        compiler.compile(true);
        compiler.resultLogger.print();
    }
}
