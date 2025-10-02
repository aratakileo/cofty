package cofty.core.parser.ast.value;

import cofty.Utils;
import cofty.core.parser.node.modifier.NodeModifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ValueExpressionObjectTest {
    @Test
    void validSingleElementOfExpressionProceed() {
        final var parseContext = Utils.parseContextOf("345");
        final var astObject = new ValueExpressionObject();

        Assertions.assertTrue(astObject.parserNode().proceedQueue(parseContext, NodeModifier.general()));
        Assertions.assertNotNull(astObject.value().value());
        Assertions.assertEquals("345", astObject.value().value().content);
    }
}