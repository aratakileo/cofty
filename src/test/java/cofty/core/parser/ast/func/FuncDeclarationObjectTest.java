package cofty.core.parser.ast.func;

import cofty.Utils;
import cofty.core.parser.node.modifier.NodeModifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class FuncDeclarationObjectTest {
    @Test
    void validNoArgumentsNoReturnableTypeNoBody() {
        final var context = Utils.parseContextOf("fun test() {}");
        final var astObject = new FuncDeclarationObject();

        Assertions.assertTrue(
                astObject.parserNode().proceedQueue(context, NodeModifier.general()),
                "the function declaration should be proceeded"
        );

        Assertions.assertEquals(
                0,
                context.messages.count(),
                "there shouldn't be any error or warning messages"
        );

        Assertions.assertNotNull(astObject.name(), "the proceeded function name shouldn't be null");

        Assertions.assertEquals(
                "test",
                astObject.name().content,
                "the function name should be `test`"
        );
    }

    @Test
    void validOneArgument() {
        final var context = Utils.parseContextOf("fun test(argument: int) {}");
        final var astObject = new FuncDeclarationObject();

        Assertions.assertTrue(
                astObject.parserNode().proceedQueue(context, NodeModifier.general()),
                "the function declaration should be proceeded"
        );

        Assertions.assertEquals(
                0,
                context.messages.count(),
                "there shouldn't be any error or warning messages"
        );

        Assertions.assertNotNull(astObject.args(), "the proceeded function argument shouldn't be null");

        Assertions.assertEquals(
                1,
                astObject.args().size(),
                "the function should has one argument"
        );
    }

    @Test
    void validReturnableTypeDeclaration() {
        final var context = Utils.parseContextOf("fun test() -> nil {}");
        final var astObject = new FuncDeclarationObject();

        Assertions.assertTrue(
                astObject.parserNode().proceedQueue(context, NodeModifier.general()),
                "the function declaration should be proceeded"
        );

        Assertions.assertEquals(
                0,
                context.messages.count(),
                "there shouldn't be any error or warning messages"
        );

        Assertions.assertNotNull(
                astObject.returnableType(),
                "the proceeded function returnable type shouldn't be null"
        );

        Assertions.assertEquals(
                "nil",
                astObject.returnableType().content,
                "the function returnable type should be `nil`"
        );
    }
}