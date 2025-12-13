package cofty.core.semantics;

import cofty.core.compiler.diagnostic.Errors;
import cofty.core.semantics.symbol.IncompletedFieldSymbol;
import cofty.core.semantics.symbol.TypeDescriptor;
import cofty.core.semantics.symbol.scope.FuncSignaturesScope;
import cofty.core.semantics.symbol.scope.IncompletedFuncScope;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ModuleQuickScannerTest {
    @Test
    void validFieldDeclarationWithSpecifiedTypeAndNoValue() {
        final var variableName = "validVariable";
        final var variableType = "int";
        final var variableSymbol = SemanticAssert.quickScan("var mut %s: %s".formatted(variableName, variableType))
                .ok()
                .hasNoDiagnosticMessages()
                .scope()
                .containsLocalName(variableName)
                .resolve(IncompletedFieldSymbol.class, variableName, variableType);

        Assertions.assertTrue(variableSymbol.isMutable(), "the variable must be specified as mutable");

        Assertions.assertFalse(
                variableSymbol.isValuePassed(),
                "the variable value must be specified as not passed"
        );
    }

    @Test
    void validFieldDeclarationWithNoSpecifiedTypeAndSimpleStringValue() {
        final var variableName = "validVariable";
        final var variableType = "str";
        final var variableSymbol = SemanticAssert.quickScan("var %s = 'uwu'".formatted(variableName))
                .ok()
                .hasNoDiagnosticMessages()
                .scope()
                .containsLocalName(variableName)
                .resolve(IncompletedFieldSymbol.class, variableName, variableType);

        Assertions.assertFalse(variableSymbol.isMutable(), "the variable must be specified as immutable");

        Assertions.assertTrue(
                variableSymbol.isValuePassed(),
                "the variable value must be specified as passed"
        );
    }

    @Test
    void validFuncDeclarationWithNoArgsNoReturnTypeEmptyBody() {
        final var funcName = "validFunc";

        SemanticAssert.quickScan("fun %s() {}".formatted(funcName))
                .ok()
                .hasNoDiagnosticMessages()
                .scope()
                .containsLocalName(funcName)
                .scope(FuncSignaturesScope.class, funcName)
                .containsChildren(1)
                .typedScope(IncompletedFuncScope.class, "0")
                .checkType(TypeDescriptor.RELATIVE_NULL)
                .containsChildren(0);
    }

    @Test
    void validFuncDeclarationWithRequiredArgAndNonRequiredArgAndWithReturnType() {
        final var funcName = "validFunc";
        final var firstArgName = "a";
        final var firstArgType = "int";
        final var secondArgName = "b";
        final var secondArgType = "bool";
        final var secondArgValue = "false";
        final var returnType = "str";

        SemanticAssert.quickScan("fun %s(%s: %s, %s = %s) -> %s {return ''}".formatted(
                funcName, firstArgName, firstArgType, secondArgName, secondArgValue, returnType
        )).ok()
                .hasNoDiagnosticMessages()
                .scope()
                .containsLocalName(funcName)
                .scope(FuncSignaturesScope.class, funcName)
                .containsChildren(1)
                .typedScope(IncompletedFuncScope.class, "0")
                .checkType(returnType)
                .containsChildren(2)
                .contains(IncompletedFieldSymbol.class, firstArgName, firstArgType)
                .contains(IncompletedFieldSymbol.class, secondArgName, secondArgType);
    }

    @Test
    void validTwoSignaturesFuncDeclaration() {
        final var funcName = "validFunc";

        SemanticAssert.quickScan("""
                        fun %s() {}
                        fun %s(a: float) -> float {return 0.2}
                        """.formatted(funcName, funcName)
        ).ok()
                .hasNoDiagnosticMessages()
                .scope()
                .containsLocalName(funcName)
                .scope(FuncSignaturesScope.class, funcName)
                .containsChildren(2);
    }

    @Test
    void invalidDuplicateName() {
        final var variableName = "duplicatedVariable";

        SemanticAssert.quickScan("""
                        var %s: int
                        var %s = false
                        """.formatted(variableName, variableName)
        ).failed().hasErrors(Errors.DUPLICATE_NAME);
    }

    @Test
    void invalidDuplicateFuncArgs() {
        final var argName = "duplicatedArg";

        SemanticAssert.quickScan("""
                        fun test(%s: int, %s: int) {}
                        """.formatted(argName, argName)
        ).failed().hasErrors(Errors.DUPLICATE_NAME);
    }

    @Test
    void invalidDuplicateFuncSignature() {
        final var funcName = "duplicatedSignature";

        SemanticAssert.quickScan("""
                        fun %s() {}
                        fun %s() {}
                        """.formatted(funcName, funcName)
        ).failed().hasErrors(Errors.DUPLICATE_FUNC_SIGNATURE);
    }
}