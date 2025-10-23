package cofty.core.parser.ast;

import cofty.Utils;
import cofty.core.parser.ast.value.PrimitiveValueObject;
import cofty.core.parser.node.modifier.NodeModifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class VarDeclarationObjectTest {
    @Test
    void validFullGeneral() {
        final var context = Utils.parseContextOf("public var mut num: int = _3_000_000");
        final var astObject = new VarDeclarationObject(false);

        Assertions.assertTrue(
                astObject.parserNode().proceedQueue(context, NodeModifier.general()),
                "`public var num: int = _3_000_000` should be proceeded"
        );

        Assertions.assertEquals(
                0,
                context.messages.count(),
                "there shouldn't be any error or warning messages"
        );

        Assertions.assertNotNull(astObject.name(), "the proceeded variable name shouldn't be null");
        Assertions.assertNotNull(astObject.value(), "the proceeded variable value shouldn't be null");
        Assertions.assertNotNull(astObject.modifiers(), "the proceeded variable modifiers shouldn't be null");

        Assertions.assertEquals(
                1,
                astObject.modifiers().modifierTokens().size(),
                "there should be 1 modifier"
        );

        Assertions.assertNotNull(
                astObject.mutable(),
                "the proceeded variable mutation declaration shouldn't be null"
        );

        Assertions.assertNotNull(
                astObject.explicitlySpecifiedType(),
                "the proceeded variable explicitly specified value type shouldn't be null"
        );

        Assertions.assertEquals("num", astObject.name().content, "the variable name should be `num`");

        Assertions.assertEquals(
                "int",
                astObject.explicitlySpecifiedType().name().segments().getFirst().content,
                "the variable explicitly specified value type should be `int`"
        );

        if (astObject.value().expr() instanceof PrimitiveValueObject primitiveValueObject) {
            Assertions.assertNotNull(primitiveValueObject.value(), "the proceeded variable value shouldn't be null");

            Assertions.assertEquals(
                    "_3_000_000",
                    primitiveValueObject.value().content,
                    "the variable value should be `_3_000_000`"
            );
        } else throw new IllegalStateException();
    }

    @Test
    void validAsFunctionArgument() {
        final var context = Utils.parseContextOf("mut num: int = _3_000_000");
        final var astObject = new VarDeclarationObject(true);

        Assertions.assertTrue(
                astObject.parserNode().proceedQueue(context, NodeModifier.general()),
                "`num: int = _3_000_000` should be proceeded"
        );

        Assertions.assertEquals(
                0,
                context.messages.count(),
                "there shouldn't be any error or warning messages"
        );

        Assertions.assertNotNull(astObject.name(), "the proceeded function argument name shouldn't be null");
        Assertions.assertNotNull(astObject.value(), "the proceeded function argument value shouldn't be null");
        Assertions.assertNotNull(astObject.modifiers(), "the proceeded function argument modifiers shouldn't be null");

        Assertions.assertNotNull(
                astObject.mutable(),
                "the proceeded function argument mutation declaration shouldn't be null"
        );

        Assertions.assertNotNull(
                astObject.explicitlySpecifiedType(),
                "the proceeded function argument explicitly specified value type shouldn't be null"
        );

        Assertions.assertEquals(
                "num",
                astObject.name().content,
                "the function argument name should be `num`"
        );

        Assertions.assertEquals(
                "int",
                astObject.explicitlySpecifiedType().name().segments().getFirst().content,
                "the function argument explicitly specified value type should be `int`"
        );

        if (astObject.value().expr() instanceof PrimitiveValueObject primitiveValueObject) {
            Assertions.assertNotNull(
                    primitiveValueObject.value(),
                    "the function argument value shouldn't be null"
            );

            Assertions.assertEquals(
                    "_3_000_000",
                    primitiveValueObject.value().content,
                    "the function argument value should be `_3_000_000`"
            );
        } else throw new IllegalStateException();
    }

    @Test
    void invalidNoTypeDeclarationNoValue() {
        final var context = Utils.parseContextOf("var variable");
        final var astObject = new VarDeclarationObject(false);

        Assertions.assertFalse(
                astObject.parserNode().proceedQueue(context, NodeModifier.general()),
                "`var variable` shouldn't be proceeded"
        );

        Assertions.assertEquals(
                1,
                context.messages.count(),
                "there should be an error"
        );

        Assertions.assertEquals(
                "SyntaxError: expected explicit type declaration or value assignment",
                context.messages.CRITICAL.get(0).content
        );
    }

    @Test
    void validPreview() {
        final var context = Utils.parseContextOf("public var");
        final var astObject = new VarDeclarationObject(false);

        Assertions.assertTrue(
                astObject.parserNode().proceedQueue(context, NodeModifier.generalAndPreview()),
                "`public var` should be previewed"
        );
    }
}