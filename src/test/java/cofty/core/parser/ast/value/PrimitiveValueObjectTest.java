package cofty.core.parser.ast.value;

import cofty.Utils;
import cofty.core.lexer.token.type.Keyword;
import cofty.core.lexer.token.type.Simple;
import cofty.core.parser.node.modifier.NodeModifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PrimitiveValueObjectTest {
    @Test
    void validIntValueProceed() {
        final var value = "_123";
        final var parseContext = Utils.parseContextOf(value);
        final var astObject = new PrimitiveValueObject();

        Assertions.assertTrue(
                astObject.parserNode().proceedQueue(parseContext, NodeModifier.general()),
                "primitive value should be proceeded"
        );

        Assertions.assertEquals(
                0,
                parseContext.messages.count(),
                "there shouldn't be any error or warning messages"
        );

        Assertions.assertNotNull(astObject.value(), "primitive value shouldn't be null");
        Assertions.assertEquals(Simple.INT, astObject.value().type, "primitive value should be int value");

        Assertions.assertEquals(
                value,
                astObject.value().content,
                String.format("primitive value should be equals `%s`", value)
        );
    }

    @Test
    void validDoubleValueProceed() {
        final var value = "69.69";
        final var parseContext = Utils.parseContextOf(value);
        final var astObject = new PrimitiveValueObject();

        Assertions.assertTrue(
                astObject.parserNode().proceedQueue(parseContext, NodeModifier.general()),
                "primitive value should be proceeded"
        );

        Assertions.assertEquals(
                0,
                parseContext.messages.count(),
                "there shouldn't be any error or warning messages"
        );

        Assertions.assertNotNull(astObject.value(), "primitive value shouldn't be null");

        Assertions.assertEquals(
                Simple.DOUBLE,
                astObject.value().type,
                "primitive value should be double value"
        );

        Assertions.assertEquals(
                value,
                astObject.value().content,
                String.format("primitive value should be equals `%s`", value)
        );
    }

    @Test
    void validStrValueProceed() {
        final var value = "'Hello world!\\n'";
        final var parseContext = Utils.parseContextOf(value);
        final var astObject = new PrimitiveValueObject();

        Assertions.assertTrue(
                astObject.parserNode().proceedQueue(parseContext, NodeModifier.general()),
                "primitive value should be proceeded"
        );

        Assertions.assertEquals(
                0,
                parseContext.messages.count(),
                "there shouldn't be any error or warning messages"
        );

        Assertions.assertNotNull(astObject.value(), "primitive value shouldn't be null");

        Assertions.assertEquals(
                Simple.STR,
                astObject.value().type,
                "primitive value should be str value"
        );

        Assertions.assertEquals(
                value,
                astObject.value().content,
                String.format("primitive value should be equals `%s`", value)
        );
    }

    @Test
    void validBooleanValueProceed() {
        final var value = "true";
        final var parseContext = Utils.parseContextOf(value);
        final var astObject = new PrimitiveValueObject();

        Assertions.assertTrue(
                astObject.parserNode().proceedQueue(parseContext, NodeModifier.general()),
                "primitive value should be proceeded"
        );

        Assertions.assertEquals(
                0,
                parseContext.messages.count(),
                "there shouldn't be any error or warning messages"
        );

        Assertions.assertNotNull(astObject.value(), "primitive value shouldn't be null");

        Assertions.assertEquals(
                Keyword.TRUE,
                astObject.value().type,
                "primitive value should be " + Keyword.TRUE
        );

        Assertions.assertEquals(
                value,
                astObject.value().content,
                String.format("primitive value should be equals `%s`", value)
        );
    }

    @Test
    void validIdValueProceed() {
        final var value = "myVar270";
        final var parseContext = Utils.parseContextOf(value);
        final var astObject = new PrimitiveValueObject();

        Assertions.assertTrue(
                astObject.parserNode().proceedQueue(parseContext, NodeModifier.general()),
                "primitive value should be proceeded"
        );

        Assertions.assertEquals(
                0,
                parseContext.messages.count(),
                "there shouldn't be any error or warning messages"
        );

        Assertions.assertNotNull(astObject.value(), "primitive value shouldn't be null");

        Assertions.assertEquals(
                Simple.WORD,
                astObject.value().type,
                "primitive value should be id value"
        );

        Assertions.assertEquals(
                value,
                astObject.value().content,
                String.format("primitive value should be equals `%s`", value)
        );
    }
}