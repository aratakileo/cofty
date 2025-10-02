package cofty.core.parser.ast.statement;

import cofty.Utils;
import cofty.core.parser.node.modifier.NodeModifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class IfStatementsObjectTest {
    @Test
    void validIfElifElseStatements() {
        final var expression = "if true {} elif true {} else {}";
        final var context = Utils.parseContextOf(expression);
        final var astObject = new IfStatementsObject();

        Assertions.assertTrue(
                astObject.parserNode().proceedQueue(context, NodeModifier.general()),
                String.format("`%s` should be proceeded", expression)
        );

        Assertions.assertEquals(
                0,
                context.messages.count(),
                "there shouldn't be any error or warning messages"
        );

        Assertions.assertNotNull(
                astObject.ifStatement().statement().value().value(),
                "the proceeded if statement expression shouldn't be null"
        );

        Assertions.assertNotNull(
                astObject.ifStatement().body().objects(),
                "the proceeded if statement body shouldn't be null"
        );

        Assertions.assertNotNull(
                astObject.elseIfStatements(),
                "the proceeded elif statement shouldn't be null"
        );

        Assertions.assertEquals(
                1,
                astObject.elseIfStatements().size(),
                "there should be one elif statement"
        );
    }

    @Test
    void validIfElseStatements() {
        final var expression = "if true {} else {}";
        final var context = Utils.parseContextOf(expression);
        final var astObject = new IfStatementsObject();

        Assertions.assertTrue(
                astObject.parserNode().proceedQueue(context, NodeModifier.general()),
                String.format("`%s` should be proceeded", expression)
        );

        Assertions.assertEquals(
                0,
                context.messages.count(),
                "there shouldn't be any error or warning messages"
        );

        Assertions.assertNotNull(
                astObject.ifStatement().statement().value().value(),
                "the proceeded if statement expression shouldn't be null"
        );

        Assertions.assertNotNull(
                astObject.ifStatement().body().objects(),
                "the proceeded if statement body shouldn't be null"
        );

        Assertions.assertNotNull(
                astObject.elseIfStatements(),
                "the proceeded elif statement shouldn't be null"
        );

        Assertions.assertEquals(
                0,
                astObject.elseIfStatements().size(),
                "there should be no elif statements"
        );
    }
}