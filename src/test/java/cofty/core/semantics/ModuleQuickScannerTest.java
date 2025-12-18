package cofty.core.semantics;

import cofty.core.compiler.diagnostic.Errors;
import cofty.core.semantics.symbol.ArgsSignature;
import cofty.core.semantics.symbol.IncompletedFieldSymbol;
import cofty.core.semantics.symbol.TypeDescriptor;
import cofty.core.semantics.symbol.scope.ClassScope;
import cofty.core.semantics.symbol.scope.FuncSignaturesScope;
import cofty.core.semantics.symbol.scope.IncompletedFuncScope;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

class ModuleQuickScannerTest {
    @Test
    void validFieldDeclarationWithSpecifiedTypeAndNoValue() {
        final var variableName = "validVariable";
        final var variableType = "int";
        final var variableSymbol = SemanticAssert.quickScan("var mut %s: %s".formatted(variableName, variableType))
                .ok()
                .hasNoDiagnosticMessages()
                .scope()
                .containsChildren(1)
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
                .containsChildren(1)
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
                .containsChildren(1)
                .containsLocalName(funcName)
                .scope(FuncSignaturesScope.class, funcName)
                .containsChildren(1)
                .typedScope(IncompletedFuncScope.class, ArgsSignature.SIGNATURES_PREFIX)
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

        final var funcScope = SemanticAssert.quickScan("fun %s(%s: %s, %s = %s) -> %s {return ''}".formatted(
                funcName, firstArgName, firstArgType, secondArgName, secondArgValue, returnType
        )).ok()
                .hasNoDiagnosticMessages()
                .scope()
                .containsChildren(1)
                .containsLocalName(funcName)
                .scope(FuncSignaturesScope.class, funcName)
                .containsChildren(1)
                .typedScope(IncompletedFuncScope.class, argsSignatureView(firstArgType))
                .checkType(returnType)
                .containsChildren(2)
                .scope;

        Assertions.assertTrue(funcScope.argsSignature.containsName(firstArgName));
        Assertions.assertEquals(funcScope.argsSignature.getType(firstArgName), TypeDescriptor.rawReference(firstArgType));

        Assertions.assertTrue(funcScope.argsSignature.containsName(firstArgName));
        Assertions.assertEquals(funcScope.argsSignature.getType(secondArgName), TypeDescriptor.rawReference(secondArgType));
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
                .containsChildren(1)
                .containsLocalName(funcName)
                .scope(FuncSignaturesScope.class, funcName)
                .containsChildren(2);
    }

    @Test
    void validClassDeclaration() {
        final var className = "ValidClass";

        SemanticAssert.quickScan("""
                        class %s {class %s {}}
                        """.formatted(className, className)
        ).ok()
                .hasNoDiagnosticMessages()
                .scope()
                .containsChildren(1)
                .containsLocalName(className)
                .scope(ClassScope.class, className)
                .containsChildren(1)
                .containsLocalName(className)
                .scope(ClassScope.class, className)
                .containsChildren(0);
    }

    @Test
    void validIgnoredNestedBodiesVariables() {
        final var variableName = "shadowVariable";

        SemanticAssert.quickScan(MessageFormat.format("""
                        fun test() '{'var {0}: int'}'
                        '{'var {0}: int'}'
                        """,
                        variableName
        )).ok()
                .hasNoDiagnosticMessages()
                .scope()
                .containsChildren(1)
                .containsLocalName("test")
                .scope(FuncSignaturesScope.class, "test")
                .containsChildren(1)
                .containsLocalName(ArgsSignature.SIGNATURES_PREFIX)
                .scope(IncompletedFuncScope.class, ArgsSignature.SIGNATURES_PREFIX)
                .containsChildren(0);
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

    @Test
    void invalidRequiredArgsOverrideDuplicate() {
        final var funcName = "duplicatedSignature";

        SemanticAssert.quickScan("""
                        fun %s(a: int) {}
                        fun %s(b: int, c = 345) {}
                        """.formatted(funcName, funcName)
        ).failed().hasErrors(Errors.DUPLICATE_FUNC_SIGNATURE);
    }

    public static @NotNull String argsSignatureView(@NotNull String @NotNull... names) {
        return ArgsSignature.viewOf(Arrays.stream(names).map(TypeDescriptor::rawReference).toList());
    }
}