package cofty.core.parser.ast;

import cofty.Utils;
import cofty.v3.core.parser.ast.ModifiersObject;
import cofty.v3.core.parser.node.modifier.NodeModifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ModifiersObjectTest {
    @Test
    void validAllModifiers() {
        final var expression = "pub priv";
        final var context = Utils.parseContextOf(expression);
        final var astObject = new ModifiersObject();

        Assertions.assertTrue(
                astObject.parserNode().proceedQueue(context, NodeModifier.general()),
                String.format("`%s` should be proceeded", expression)
        );

        Assertions.assertEquals(
                0,
                context.messages.count(),
                "there shouldn't be any error or warning messages"
        );

        Assertions.assertEquals(
                2,
                astObject.modifierTokens().size(),
                "there should be 2 modifiers"
        );
    }
}