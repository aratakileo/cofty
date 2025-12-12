package cofty.core.semantics;

import cofty.Utils;
import cofty.core.semantics.symbol.IncompletedFieldSymbol;
import cofty.core.semantics.symbol.scope.FuncSignaturesScope;
import cofty.core.semantics.symbol.scope.IncompletedFuncScope;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ModuleQuickAnalyzerTest {
    @Test
    void validFieldDeclarationWithSpecifiedTypeAndNoValue() {
        final var variableName = "validVariable";
        final var variableType = "int";
        final var moduleAnalyzeContext = Utils.moduleContextOf(String.format(
                "var mut %s: %s",
                variableName,
                variableType
        ));

        final var analyzer = new ModuleQuickAnalyzer(moduleAnalyzeContext);
        final var analyzeResult = analyzer.analyze();

        Assertions.assertTrue(moduleAnalyzeContext.messages.engine.isEmpty());
        Assertions.assertTrue(analyzeResult, "the result of the semantics analyze must be successful");

        Assertions.assertTrue(
                moduleAnalyzeContext.scope.containsName(variableName),
                String.format("the module scope must contains the variable name `%s`", variableName)
        );

        Assertions.assertInstanceOf(
                IncompletedFieldSymbol.class,
                moduleAnalyzeContext.scope.resolveOrThrow(variableName),
                String.format("the defined variable `%s` must be specified as incompleted", variableName)
        );

        final var variableSymbol = (IncompletedFieldSymbol)moduleAnalyzeContext.scope.resolveOrThrow(variableName);

        Assertions.assertEquals(1, variableSymbol.valueType().name.size());

        Assertions.assertEquals(
                variableType,
                variableSymbol.valueType().name.getFirst().content,
                String.format("the variable type must be specified as `%s`", variableType)
        );

        Assertions.assertTrue(variableSymbol.isMutable(), "the variable must be specified as mutable");

        Assertions.assertFalse(
                variableSymbol.isValuePassed(),
                "the variable value must be specified as not passed"
        );
    }

    @Test
    void validFieldDeclarationWithNoSpecifiedTypeAndSimpleStringValue() {
        final var variableName = "validVariable";
        final var moduleAnalyzeContext = Utils.moduleContextOf(String.format("var %s = 'uwu'", variableName));
        final var analyzer = new ModuleQuickAnalyzer(moduleAnalyzeContext);
        final var analyzeResult = analyzer.analyze();

        Assertions.assertTrue(moduleAnalyzeContext.messages.engine.isEmpty());
        Assertions.assertTrue(analyzeResult, "the result of the semantics analyze must be successful");

        Assertions.assertTrue(
                moduleAnalyzeContext.scope.containsName(variableName),
                String.format("the module scope must contains the variable name `%s`", variableName)
        );

        Assertions.assertInstanceOf(
                IncompletedFieldSymbol.class,
                moduleAnalyzeContext.scope.resolveOrThrow(variableName),
                String.format("the defined variable `%s` must be specified as incompleted", variableName)
        );

        final var variableSymbol = (IncompletedFieldSymbol)moduleAnalyzeContext.scope.resolveOrThrow(variableName);

        Assertions.assertNull(variableSymbol.valueType());
        Assertions.assertFalse(variableSymbol.isMutable(), "the variable must be specified as immutable");

        Assertions.assertTrue(
                variableSymbol.isValuePassed(),
                "the variable value must be specified as passed"
        );
    }

    @Test
    void validFuncDeclarationWithNoArgsNoReturnTypeEmptyBody() {
        final var funcName = "validFunc";
        final var moduleAnalyzeContext = Utils.moduleContextOf(String.format("fun %s() {}", funcName));
        final var analyzer = new ModuleQuickAnalyzer(moduleAnalyzeContext);
        final var analyzeResult = analyzer.analyze();

        Assertions.assertTrue(moduleAnalyzeContext.messages.engine.isEmpty());
        Assertions.assertTrue(analyzeResult, "the result of the semantics analyze must be successful");

        Assertions.assertTrue(
                moduleAnalyzeContext.scope.containsName(funcName),
                String.format("the module scope must contains the function name `%s`", funcName)
        );

        Assertions.assertInstanceOf(FuncSignaturesScope.class, moduleAnalyzeContext.scope.resolveOrThrow(funcName));

        final var funcSignaturesScope = (FuncSignaturesScope)moduleAnalyzeContext.scope.resolveOrThrow(funcName);

        Assertions.assertEquals(1, funcSignaturesScope.childNames().size());

        Assertions.assertInstanceOf(
                IncompletedFuncScope.class,
                funcSignaturesScope.resolve("0"),
                String.format("the defined function `%s` must be specified as incompleted", funcName)
        );

        final var funcScope = (IncompletedFuncScope)funcSignaturesScope.resolveOrThrow("0");

        Assertions.assertNull(funcScope.valueType());

        Assertions.assertEquals(
                0,
                funcScope.getArgSignatures().size(),
                "the function must have no arguments"
        );
    }
}