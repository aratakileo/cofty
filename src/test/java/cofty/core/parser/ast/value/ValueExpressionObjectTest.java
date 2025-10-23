package cofty.core.parser.ast.value;

import cofty.Utils;
import cofty.core.parser.ast.value.expr.ValueExpressionObject;
import cofty.core.parser.node.modifier.NodeModifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Objects;

class ValueExpressionObjectTest {
    @Test
    void validSingleElementOfExpressionProceed() {
        final var parseContext = Utils.parseContextOf("345");
        final var astObject = new ValueExpressionObject();

        Assertions.assertTrue(astObject.parserNode().proceedQueue(parseContext, NodeModifier.general()));

        if (astObject.expr() instanceof PrimitiveValueObject primitiveValueObject) {
            Assertions.assertNotNull(primitiveValueObject.value());
            Assertions.assertEquals("345", Objects.requireNonNull(primitiveValueObject.value()).content);
        } else throw new RuntimeException();
    }
}