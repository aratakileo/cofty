package cofty.core.parser.ast;

import cofty.Utils;
import cofty.core.parser.node.modifier.NodeModifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SubBodyObjectTest {
    @Test
    void validBody() {
        final var context = Utils.parseContextOf("static public {public var num = 10}");

        final var astObject = new SubBodyObject();

        Assertions.assertTrue(
                astObject.parserNode().proceedQueue(context, NodeModifier.general()),
                "sub body expressions should be proceeded"
        );

        Assertions.assertEquals(
                0,
                context.messages.count(),
                "there shouldn't be any error or warning messages"
        );

        Assertions.assertEquals(
                1,
                astObject.body().objects().size(),
                "there should be one ast objects of body"
        );

        Assertions.assertEquals(
                2,
                astObject.modifiers().modifierTokens().size(),
                "there should be 2 modifiers"
        );
    }
}