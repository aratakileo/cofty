package cofty.core.parser.ast.value;

import cofty.Utils;
import cofty.v3.core.parser.ast.value.PrimitiveValueObject;
import cofty.v3.core.parser.node.modifier.NodeModifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PrimitiveValueObjectTest {
    @Test
    void validIntValueProceed() {
        final var parseContext = Utils.parseContextOf("_123");
        final var astObject = new PrimitiveValueObject();

        Assertions.assertTrue(astObject.parserNode().proceedQueue(parseContext, NodeModifier.general()));
        Assertions.assertNotNull(astObject.value());
        Assertions.assertEquals("_123", astObject.value().content);
    }
}