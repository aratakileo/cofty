package cofty.core.parser.ast;

import cofty.Utils;
import cofty.v3.core.parser.ast.ClassObject;
import cofty.v3.core.parser.node.modifier.NodeModifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ClassObjectTest {
    @Test
    void validNoBody() {
        final var context = Utils.parseContextOf("pub cls Test {}");
        final var astObject = new ClassObject();

        Assertions.assertTrue(
                astObject.parserNode().proceedQueue(context, NodeModifier.general()),
                "the class declaration should be proceeded"
        );

        Assertions.assertEquals(
                0,
                context.messages.count(),
                "there shouldn't be any error or warning messages"
        );

        Assertions.assertNotNull(astObject.name(), "the proceeded class name shouldn't be null");

        Assertions.assertEquals(
                "Test",
                astObject.name().content,
                "the function name should be `test`"
        );

        Assertions.assertEquals(
                1,
                astObject.modifiers().modifiers().size(),
                "there should be one modifier"
        );
    }
}