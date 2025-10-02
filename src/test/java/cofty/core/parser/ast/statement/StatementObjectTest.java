package cofty.core.parser.ast.statement;

import cofty.Utils;
import cofty.core.lexer.token.type.Keyword;
import cofty.core.parser.node.modifier.NodeModifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class StatementObjectTest {
    @Test
    void validNoParenthesisIfStatement() {
        final var context = Utils.parseContextOf("if true {}");
        final var astObject = new StatementObject(Keyword.IF);

        Assertions.assertTrue(
                astObject.parserNode().proceedQueue(context, NodeModifier.general()),
                "`if true {}` should be proceeded"
        );

        Assertions.assertEquals(
                0,
                context.messages.count(),
                "there shouldn't be any error or warning messages"
        );

        Assertions.assertNotNull(
                astObject.statement().value().value(),
                "the proceeded if statement expression shouldn't be null"
        );
    }

    @Test
    void validIfStatementSingleLineBody() {
        final var context = Utils.parseContextOf("if (true) num = 0\nnum = 1");
        final var astObject = new StatementObject(Keyword.IF);

        Assertions.assertTrue(
                astObject.parserNode().proceedQueue(context, NodeModifier.general()),
                "`if (true) num = 0` should be proceeded"
        );

        Assertions.assertEquals(
                0,
                context.messages.count(),
                "there shouldn't be any error or warning messages"
        );

        Assertions.assertNotNull(
                astObject.statement().value().value(),
                "the proceeded if statement expression shouldn't be null"
        );

        Assertions.assertNotNull(
                astObject.body().objects(),
                "the proceeded if statement body shouldn't be null"
        );

        Assertions.assertEquals(
                1,
                astObject.body().objects().size(),
                "there should be only one element in the if statement body"
        );
    }
}